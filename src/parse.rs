use std::fs::File;
use std::io::{prelude::*, BufReader};
use crate::util::HasFirst;
use crate::util::IterableExt;
use crate::mappings::{ClassMapping, MethodMapping, ArgumentMapping, FieldMapping};
use std::path::Path;

trait Mapping {
    fn name_obf(&self) -> String;
    fn name_deobf(&self) -> String;
}

impl ClassMapping {
    pub fn parse<P:AsRef<Path>>(from : P) -> ClassMapping{
        let reader = BufReader::new(File::open(from).expect("Invalid path for mappings"));

        let mut current_indent_level: i32;

        let mut top_level_class: ClassMapping = ClassMapping::empty();

        for full_line in reader.lines() {
            let string = full_line.unwrap();
            current_indent_level = string.chars().count_occurs(|&c| c == '\t') as i32;
            let without_tab = string.replace("\t", "");
            let line: Vec<&str> = without_tab.split(' ').collect::<Vec<&str>>();

            match line[0] {
                "CLASS" => {
                    let current_class = ClassMapping {
                        obf_name: line[1].to_string(),
                        deobf_name: (if line.len() >= 3 { line[2] } else { "" }).to_string(),
                        methods: vec![],
                        fields: vec![],
                        inner_classes: vec![],
                    };

                    if current_indent_level == 0 {
                        top_level_class = current_class;
                    } else {
                        get_class_in(current_indent_level - 1, &mut top_level_class)
                            .inner_classes.push(current_class);
                    }
                }
                "FIELD" => add_field(current_indent_level, &mut top_level_class, line),
                "METHOD" => add_method(current_indent_level, &mut top_level_class, line),
                "ARG" => add_argument(current_indent_level, &mut top_level_class, line),
                _ => panic!("Unexpected line prefix: {}", line[0])
            }
        }

        top_level_class
    }
}



fn add_method(current_indent_level: i32, mut top_level_class: &mut ClassMapping, line: Vec<&str>) {
    let mapping_exists = line[2].first() != '(';
    let current_method = MethodMapping {
        obf_name: line[1].to_string(),
        deobf_name: (if mapping_exists { line[2] } else { "" }).to_string(),
        descriptor: (if mapping_exists { line[3] } else { line[2] }).to_string(),
        args: Vec::new(),
    };

    let class_in = get_class_in(current_indent_level - 1, &mut top_level_class);
    class_in.methods.push(current_method);
}


fn get_class_in(indent_level: i32, mapping: &mut ClassMapping) -> &mut ClassMapping {
    let mut next = mapping;
    for _ in 1..=indent_level {
        next = next.inner_classes.last_mut().unwrap();
    }
    next
}

fn add_argument(indent_level: i32, top_level_class: &mut ClassMapping, parts: Vec<&str>) {
    let current_arg = ArgumentMapping {
        pos: parts[1].parse().unwrap(),
        name: parts[2].to_string(),
    };
    get_class_in(indent_level - 2, top_level_class).methods.last_mut().expect("No method for holding argument ").args.push(current_arg);
}

fn add_field(indent_level: i32, top_level_class: &mut ClassMapping, parts: Vec<&str>) {
    let current_field = FieldMapping {
        obf_name: parts[1].to_string(),
        deobf_name: parts[2].to_string(),
        descriptor: parts[3].to_string(),
    };
    get_class_in(indent_level - 1, top_level_class).fields.push(current_field);
}

