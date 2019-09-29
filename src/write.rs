use crate::mappings::ClassMapping;
use std::fs::File;
use std::io::{LineWriter, Write};
use crate::util::NewLineWriter;

impl ClassMapping {
    pub fn write(&self, to_file: File) {
        let mut writer = LineWriter::new(to_file);
        self.write_recur(&mut writer, 0);
    }

    fn write_recur<W: Write>(&self, writer: &mut LineWriter<W>, indent_level: usize) {
        let indent = "\t".repeat(indent_level);
        let string = f!(" {}", self.name_deobf);
        writer.write_line(
            f!("{}CLASS {}{}",
            indent, self.name_obf,if self.name_deobf.is_empty() {""} else {
                string.as_ref()
            })
        );
        for class in &self.inner_classes {
            class.write_recur(writer, indent_level + 1);
        }
        for field in &self.fields {
            writer.write_line(f!("\t{}FIELD {} {} {}", indent,field.name_obf,field.name_deobf,field.descriptor));
        }
        for method in &self.methods {
            let mapping_exists = method.name_deobf != "";
            let ending: &String;
            let ending_value : String;
            if mapping_exists {
                ending_value = f!("{} {}", method.name_deobf,method.descriptor);
                ending = &ending_value;
            } else {
                ending = &method.descriptor
            };
            writer.write_line(f!(
                "\t{}METHOD {} {}", indent, method.name_obf,
                                 ending)
            );

            for arg in method.args.iter() {
                writer.write_line(f!("\t\t{}ARG {} {}",indent,arg.pos,arg.name));
            }
        }
    }
}