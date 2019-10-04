use crate::git::{YarnRepo, RELATIVE_MAPPINGS_DIR};
use walkdir::{WalkDir, DirEntry};
use std::path::PathBuf;
use crate::mappings::{ClassMapping, Mapping};
use serenity::http::AttachmentType::File;
use crate::util::VecExt;
use std::ops::Add;

pub struct RenameResult {
    /// Usually the same as the old mapping location. Can differ when renaming/naming a top-level class
    pub new_mapping_location: String,
    pub human_old_name: String,
    pub human_new_name: String,
}


pub trait Rename {
    fn matches_file_name(&self, file_name_without_ext: &str) -> bool;
    fn matches_mappings_file(&self, mappings_file: &mut ClassMapping) -> bool;

    fn rename(&self, mapping: &mut ClassMapping) -> RenameResult;
}
//
//pub trait Obfsucateable {
////    fn name(self) -> String;
//    fn search_obf(self) -> bool;
//
//    fn target_name<M: Mapping>(&self, mapping: &M) -> String {
//        if self.search_obf() {
//            mapping.obf_name()
//        } else {
//            mapping.deobf_name()
//        }
//    }
//}
#[derive(Clone)]
pub struct ClassPath {
    pub class_name: String,
    /// in the context of old_name, when the class is net/minecraft/foo/bar/block then package can be foo/bar
    pub package: Option<String>,
}

impl ClassPath {
    fn join(&self) -> String {
        if let Some(package) = &self.package {
            format!("{}/{}", package, self.class_name)
        } else {
            self.class_name.clone()
        }
    }
}


pub struct ClassRename {
    pub old_name: ClassPath,
    pub new_name: ClassPath,
    pub inner_class: Option<Box<ClassRename>>,
    /// False when renaming by deobf for example Block -> Blocker
    /// True when renaming by obf for example class_123 -> net/minecraft/foo/bar/TheNamedClass
    pub search_obf: bool,
}

pub struct MethodRename { pub  old_name: String, pub  new_name: String, pub class_in: ClassRename, pub search_obf: bool }

pub struct FieldRename { pub  old_name: String, pub  new_name: String, pub class_in: ClassRename, pub search_obf: bool }

pub struct ParameterRename { pub  old_name: String, pub  new_name: String, pub method_in: MethodRename }

//
//impl Obfsucateable for ClassRename {
////    fn name(self) -> String {
////        self.top_level_class_name
////    }
//
//    fn search_obf(self) -> bool {
//        self.search_obf
//    }
//}
//
//impl Obfsucateable for MethodRename {
////    fn name(self) -> String {
////        self.name
////    }
//
//    fn search_obf(self) -> bool {
//        self.search_obf
//    }
//}
//
//impl Obfsucateable for FieldRename {
////    fn name(self) -> String {
////        self.name
////    }
//
//    fn search_obf(self) -> bool {
//        self.search_obf
//    }
//}

impl ClassRename {
    fn target_name<'a>(&self, mapping: &'a ClassMapping) -> &'a String {
        if self.search_obf { &mapping.obf_name } else { &mapping.deobf_name }
    }

    fn matches_name(&self, mapping: &ClassMapping) -> bool {
        let mapping_path = self.target_name(mapping);

        let split: Vec<&str> = mapping_path.split("/").collect();
        let mapping_name = split.last().unwrap_or(&"");

        if let Some(package) = &self.old_name.package {
            println!("Checking package {}",package);
            let mapping_package: String = split[..split.len() - 1].join("/");
            return mapping_name.to_string() == self.old_name.class_name
                && mapping_package.ends_with(package);
        } else {
            return mapping_name.to_string() == self.old_name.class_name;
        }
    }

    // Trivial for outer classes, more complicated (recursive) for inner classes
    // Note: won't work too well when there are 2 inner classes with the same name, don't think those exist though.
    fn find<'a>(&self, mappings_file: &'a mut ClassMapping) -> Option<&'a mut ClassMapping> {
        if !self.matches_name(mappings_file) { return None; }

        if let Some(inner_class) = &self.inner_class {
            for class in &mut mappings_file.inner_classes {
                let found = inner_class.find(class);
                if found.is_some() { return found; }
            }
            return None;
        } else {
            return Some(mappings_file);
        }
    }

    pub fn human_readable_old_name(&self) -> String {
        let top_level_name = &self.old_name.class_name;
        if let Some(inner_class) = &self.inner_class {
            format!("{}${}", top_level_name, inner_class.human_readable_old_name())
        } else {
            top_level_name.clone()
        }
    }

    pub fn human_readable_new_name(&self) -> String {
        let top_level_name = &self.new_name.class_name;
        if let Some(inner_class) = &self.inner_class {
            format!("{}${}", top_level_name, inner_class.human_readable_new_name())
        } else {
            top_level_name.clone()
        }
    }
}

struct MyStruct { string: String }


impl Rename for ClassRename {
    fn matches_file_name(&self, file_name_without_ext: &str) -> bool {
        self.old_name.class_name == file_name_without_ext
    }

    fn matches_mappings_file(&self, mappings_file: &mut ClassMapping) -> bool {
        self.find(mappings_file).is_some()
    }

    //TODO: test with obf + outer, obf + inner, deobf + outer, deobf + inner, and test qualifying
    fn rename(&self, mapping: &mut ClassMapping) -> RenameResult {
        let old_package = if self.search_obf { mapping.obf_package_name().clone() } else { mapping.deobf_package_name().clone() };

        let new_package = if let Some(new_package) = &self.new_name.package {
            // If the new name has a package attach to it, we use that package instead
            new_package
        } else {
            // If there is no package attached, we keep using the same package
            &old_package
        };

        let new_mappings_path = format!("{}/{}/{}.mapping", RELATIVE_MAPPINGS_DIR, new_package, &self.new_name.class_name);

        let to_rename = self.find(mapping)
            .expect("When renaming a class it is expected for the searched class to be known to exist in the file");

        let human_old_name = if old_package != "" {
            format!("{}/{}", old_package, self.human_readable_old_name())
        }else {self.human_readable_old_name()};
        let new_name = self.new_name.join();
        let human_new_name = if new_package != "" {
            format!("{}/{}", new_package, self.human_readable_new_name())
        }else {self.human_readable_new_name()};

        to_rename.deobf_name = new_name;

        RenameResult { new_mapping_location: new_mappings_path, human_new_name, human_old_name }
    }
}


pub const MAPPING_EXT_LENGTH: usize = 8;

impl YarnRepo {
    pub fn find(query: &Box<dyn Rename>) -> Vec<PathBuf> {
        WalkDir::new(YarnRepo::get_mappings_directory())
            .into_iter()
            .filter_map(Result::ok)
            .filter(|file: &DirEntry| {
                if !(file.file_type().is_dir()) {
                    let name = file.file_name().to_str().unwrap();
                    query.matches_file_name(&name[..name.len() - MAPPING_EXT_LENGTH])
                } else { false }
            })
            .map(|file: DirEntry|
                file.into_path()
            ).collect()
    }

//    pub fn find_class_location(class: &ClassQuery) -> Vec<PathBuf> {
//        let top_level_class = class.top_level_class();
//        WalkDir::new(YarnRepo::get_mappings_directory())
//            .into_iter()
//            .filter_map(Result::ok)
//            .filter(|file: &DirEntry| {
//                if !(file.file_type().is_dir()) {
//                    let name = file.file_name().to_str().unwrap();
//                    name[..name.len() - MAPPING_EXT_LENGTH] == top_level_class.name
//                } else { false }
//            })
//            .map(|file: DirEntry|
//                file.into_path()
//            ).collect()
//    }

//    pub fn find_method(method : Method) ->
}