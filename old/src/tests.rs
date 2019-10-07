// Note this useful idiom: importing names from outer (for mod tests) scope.
use super::*;
use crate::mappings::ClassMapping;
use std::fs::{File, read_to_string};
use crate::util::{get_test_resource, ReadContentsExt};
use std::io::Read;
use crate::query::{ClassRename, ClassPath};

#[test]
fn test_read_write() {
    let original = "Block.mapping";
    let new = "Block_new.mapping";
    let mappings = ClassMapping::parse(
        get_test_resource(original));
    mappings.write(File::create(new).unwrap());
    assert_eq!(File::open(get_test_resource(original)).unwrap().read_contents(), read_to_string(new).unwrap());
}

#[test]
fn test_human_readable_old_name() {
    let innerest = ClassRename {
        old_name: ClassPath { class_name: "BAZ".to_string(), package: None },
        new_name: ClassPath { class_name: "".to_string(), package: None },
        inner_class: None,
        search_obf: false,
    };

    let inner = ClassRename {
        old_name: ClassPath { class_name: "BAR".to_string(), package: None },
        new_name: ClassPath { class_name: "".to_string(), package: None },
        inner_class: Some(Box::new(ClassRename {
            old_name: ClassPath { class_name: "BAZ".to_string(), package: None },
            new_name: ClassPath { class_name: "".to_string(), package: None },
            inner_class: None,
            search_obf: false,
        }))
        ,
        search_obf: false,
    };

    let rename = ClassRename {
        old_name: ClassPath { class_name: "FOO".to_string(), package: None },
        new_name: ClassPath { class_name: "".to_string(), package: None },
        inner_class: Some(Box::new(ClassRename {
            old_name: ClassPath { class_name: "BAR".to_string(), package: None },
            new_name: ClassPath { class_name: "".to_string(), package: None },
            inner_class: Some(Box::new(ClassRename {
                old_name: ClassPath { class_name: "BAZ".to_string(), package: None },
                new_name: ClassPath { class_name: "".to_string(), package: None },
                inner_class: None,
                search_obf: false,
            }))
            ,
            search_obf: false,
        }))
        ,
        search_obf: false,
    };

    assert_eq!(innerest.human_readable_old_name(), "BAZ");
    assert_eq!(inner.human_readable_old_name(), "BAR$BAZ");
    assert_eq!(rename.human_readable_old_name(), "FOO$BAR$BAZ");
}