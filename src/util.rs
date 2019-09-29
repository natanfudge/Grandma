use core::ops;
use std::io::{LineWriter, Write, Read};
use std::fs::File;
use std::path::{PathBuf, Path};

pub trait HasFirst<T> {
    fn first(&self) -> T;
}

impl HasFirst<char> for &str {
    fn first(&self) -> char {
        self.chars().nth(0).unwrap()
    }
}

pub trait IterableExt<T> {
    fn count_occurs(self, predicate: fn(&T) -> bool) -> usize;
//    fn find_mut(&mut self, predicate: fn(&T) -> bool) ->Option<&mut T>;
}

impl<I: Iterator> IterableExt<I::Item> for I {
    fn count_occurs(self, predicate: fn(&I::Item) -> bool) -> usize {
        self.filter(predicate).count()
    }

//    fn find_mut(&mut self, predicate: fn(&I::Item) -> bool) -> Option<&mut I::Item> {
//        while let Some(x) = self.next() {
//            if predicate(x) {
//                return x;
//            }
//        }
//
//        self.try_for_each(move |x| {
//            if predicate(&x) { LoopState::Break(x) }
//            else { LoopState::Continue(()) }
//        }).break_value()
//
////        for i in 1..self.enumerate(){
////            let value = &self
////        }
////        for item in self{
////            if predicate(item) {
////                return Some(item);
////            }
////        }
////
////        None
//    }
}



pub trait NewLineWriter {
    fn write_line(&mut self, str: String);
}


//#[macro_use]
//macro_rules! f {
//    
//}

impl<W: Write> NewLineWriter for LineWriter<W> {
    fn write_line(&mut self, string: String) {
        self.write_all(f!("{}\n", string).as_ref());
    }
}

pub fn get_test_resource(name : &str) -> File{
    let mut dir = Path::new(env!("CARGO_MANIFEST_DIR"));
    let mut path = dir.join("resources").join("test").join(name);
    File::open(path).unwrap_or_else(|_| panic!("Could not find test resource {}", name))
}

pub trait ReadContentsExt{
    fn read_contents(&mut self) -> String;
}

impl ReadContentsExt for File{
    fn read_contents(&mut self) -> String{
        let mut buffer = String::new();
        self.read_to_string(&mut buffer).expect("Could not read file.");
        buffer
    }
}

