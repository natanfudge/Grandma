pub trait Query {
    fn matches_file(file_name: &str);
    // Returns the old name and the new name
    fn rename(mapping : &mut ClassMapping) -> (&str,&str);
}

pub struct Class { pub name: String, pub class_in: Option<Box<Class>> }

impl Class {
    //    fn get_class_in(indent_level: i32, mapping: &mut ClassMapping) -> &mut ClassMapping {
//    let mut next = mapping;
//    for _ in 1..=indent_level {
//        next = next.inner_classes.last_mut().unwrap();
//    }
//    next
//}
    pub fn top_level_class(&self) -> &Class {
        let mut next = self;
        while let Some(class_in) = &next.class_in {
            next = class_in;
        }
        next
    }
}

//TODO: implement find_x for these
pub struct Method { pub name: String, pub class_in: Class }

pub struct Field { pub name: String, pub class_in: Class }

pub struct Parameter { pub name: String, pub method_in: Method }


const MAPPING_EXT_LENGTH: usize = 8;

impl YarnRepo {
//    pub fn get_current_mappings() -> Vec<ClassMapping> {
//        WalkDir::new(YarnRepo::get_mappings_directory())
//            .into_iter()
//            .filter_map(Result::ok)
//            .filter(|file: &DirEntry| !file.file_type().is_dir())
//            .map(|file: DirEntry|
//                ClassMapping::parse(File::open(file.into_path()).expect("Could not open file"))
//            ).collect()
//    }

    fn test() {
        let inner = Class { name: "inner".to_string(), class_in: None };
        let class = Class { name: "test".to_string(), class_in: Some(Box::new(inner)) };
    }

    pub fn find_class_location(class: &Class) -> Vec<PathBuf> {
        let top_level_class = class.top_level_class();
        println!("Top level class = {}", top_level_class.name);
        WalkDir::new(YarnRepo::get_mappings_directory())
            .into_iter()
            .filter_map(Result::ok)
            .filter(|file: &DirEntry| {
//                if !file.file_type().is_dir() {
//
//                }

                if file.file_type().is_dir() { false } else {
                    let name = file.file_name().to_str().unwrap();
                    name[..name.len() - MAPPING_EXT_LENGTH] == top_level_class.name
                }
            })
            .map(|file: DirEntry|
                file.into_path()
            ).collect()
    }

//    pub fn find_method(method : Method) ->
}