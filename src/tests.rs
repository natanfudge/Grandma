// Note this useful idiom: importing names from outer (for mod tests) scope.
use super::*;
use crate::mappings::ClassMapping;
use std::fs::{File, read_to_string};
use crate::util::{get_test_resource, ReadContentsExt};
use std::io::Read;

#[test]
fn test_read_write() {
    let original = "Block.mapping";
    let new = "Block_new.mapping";
    let mappings = ClassMapping::parse(
        get_test_resource(original));
    mappings.write(File::create(new).unwrap());
    assert_eq!(get_test_resource(original).read_contents(), read_to_string(new).unwrap());
}

