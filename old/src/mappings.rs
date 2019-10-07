use crate::git::YarnRepo;
use walkdir::{WalkDir, DirEntry};
use std::fs::File;
use std::path::PathBuf;
use std::str::FromStr;

#[derive(Debug)]
pub struct ClassMapping {
    pub obf_name: String,
    pub deobf_name: String,
    pub inner_classes: Vec<ClassMapping>,
    pub methods: Vec<MethodMapping>,
    pub fields: Vec<FieldMapping>,
}


impl ClassMapping {
    pub fn empty() -> ClassMapping {
        ClassMapping {
            obf_name: "".to_string(),
            deobf_name: "".to_string(),
            inner_classes: vec![],
            methods: vec![],
            fields: vec![],
        }
    }
    //
//    pub fn deobf_class_name(&self) -> &str {
//        let split: Vec<&str> = self.deobf_name.split('/').collect::<Vec<&str>>();
//        *split.last().unwrap_or(&"")
//    }
//
    pub fn deobf_package_name(&self) -> String {
        let split: Vec<&str> = self.deobf_name.split('/').collect::<Vec<&str>>();
        split[..split.len() - 1].join("/")
    }

    pub fn obf_package_name(&self) -> String {
        let split: Vec<&str> = self.obf_name.split('/').collect::<Vec<&str>>();
        split[..split.len() - 1].join("/")
    }

    pub fn path_in_mappings_dir(&self) -> PathBuf {
        let name = if self.deobf_name != "" {
            &self.deobf_name
        } else {
            &self.obf_name
        };

        PathBuf::from_str(fs!("mappings/{}.mapping",name)).unwrap()
    }
}


#[derive(Debug)]
pub struct MethodMapping { pub  obf_name: String, pub deobf_name: String, pub descriptor: String, pub args: Vec<ArgumentMapping> }

#[derive(Debug)]
pub struct FieldMapping { pub obf_name: String, pub  deobf_name: String, pub descriptor: String }

#[derive(Debug)]
pub struct ArgumentMapping { pub pos: i32, pub name: String }

pub trait Mapping {
    fn obf_name(self) -> String;
    fn deobf_name(self) -> String;
    fn set_deobf(&mut self, set_to: String);
}

impl Mapping for ClassMapping {
    fn obf_name(self) -> String {
        self.obf_name
    }
    fn deobf_name(self) -> String {
        self.deobf_name
    }
    fn set_deobf(&mut self, set_to: String) {
        self.deobf_name = set_to;
    }
}

impl Mapping for MethodMapping {
    fn obf_name(self) -> String {
        self.obf_name
    }
    fn deobf_name(self) -> String {
        self.deobf_name
    }
    fn set_deobf(&mut self, set_to: String) {
        self.deobf_name = set_to;
    }
}

impl Mapping for FieldMapping {
    fn obf_name(self) -> String {
        self.obf_name
    }
    fn deobf_name(self) -> String {
        self.deobf_name
    }
    fn set_deobf(&mut self, set_to: String) {
        self.deobf_name = set_to;
    }
}


