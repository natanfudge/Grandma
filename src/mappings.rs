use crate::git::YarnRepo;
use walkdir::{WalkDir, DirEntry};
use std::fs::File;

#[derive(Debug)]
pub struct ClassMapping {
    pub name_obf: String,
    pub name_deobf: String,
    pub inner_classes: Vec<ClassMapping>,
    pub methods: Vec<MethodMapping>,
    pub fields: Vec<FieldMapping>,
}

impl ClassMapping {
    pub fn empty() -> ClassMapping {
        ClassMapping {
            name_obf: "".to_string(),
            name_deobf: "".to_string(),
            inner_classes: vec![],
            methods: vec![],
            fields: vec![],
        }
    }

    pub fn deobf_class_name(&self) -> &str {
        let split: Vec<&str> = self.name_deobf.split('/').collect::<Vec<&str>>();
        *split.last().unwrap_or(&"")
    }
    
    pub fn deobf_package_name(&self) -> String{
        let split: Vec<&str> = self.name_deobf.split('/').collect::<Vec<&str>>();
        split[..split.len() - 1].join("/")
    }
    
    
}


#[derive(Debug)]
pub struct MethodMapping { pub name_obf: String, pub name_deobf: String, pub descriptor: String, pub args: Vec<ArgumentMapping> }

#[derive(Debug)]
pub struct FieldMapping { pub name_obf: String, pub name_deobf: String, pub descriptor: String }

#[derive(Debug)]
pub struct ArgumentMapping { pub pos: i32, pub name: String }

impl YarnRepo{
    pub fn get_current_mappings() -> Vec<ClassMapping>{
        WalkDir::new(YarnRepo::get_mappings_directory())
            .into_iter()
            .filter_map(Result::ok)
            .filter(|file: &DirEntry| !file.file_type().is_dir())
            .map(|file: DirEntry|
                ClassMapping::parse(File::open(file.into_path()).expect("Could not open file"))
            ).collect()
    }


}