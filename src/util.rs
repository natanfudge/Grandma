use core::ops;
use std::io::{LineWriter, Write, Read};
use std::fs::File;
use std::path::{PathBuf, Path};
use std::iter::Map;

pub trait HasFirst<T> {
    fn first(&self) -> T;
}

impl HasFirst<char> for &str {
    fn first(&self) -> char {
        self.chars().nth(0).unwrap()
    }
}


pub trait NewLineWriter {
    fn write_line(&mut self, str: String);
}


impl<W: Write> NewLineWriter for LineWriter<W> {
    fn write_line(&mut self, string: String) {
        self.write_all(f!("{}\n", string).as_ref()).unwrap();
    }
}

#[allow(dead_code)]
pub fn get_test_resource(name: &str) -> PathBuf {
    let dir = Path::new(env!("CARGO_MANIFEST_DIR"));
   dir.join("resources").join("test").join(name)
}

pub fn get_resource(name: &str) -> PathBuf {
    let dir = Path::new(env!("CARGO_MANIFEST_DIR"));
    dir.join("resources").join("main").join(name)
}

pub trait ReadContentsExt {
    fn read_contents(&mut self) -> String;
}

impl ReadContentsExt for File {
    fn read_contents(&mut self) -> String {
        let mut buffer = String::new();
        self.read_to_string(&mut buffer).expect("Could not read file.");
        buffer
    }
}

pub trait IterableExt<T> {
    fn count_occurs(self, predicate: fn(&T) -> bool) -> usize;
}

impl<I: Iterator> IterableExt<I::Item> for I {
    fn count_occurs(self, predicate: fn(&I::Item) -> bool) -> usize {
        self.filter(predicate).count()
    }
}

//TOOD: expand to iterable that returns the type of self
pub trait VecExt<E> {
    fn map<R, F>(self, mapping: F) -> Vec<R> where F: Fn(E) -> R;
    fn filter<F>(self, predicate: F) -> Vec<E> where F: Fn(&E) -> bool;

    //    fn map_mut<'a, R, F>(&'a mut self, mapping: F) -> Vec<&'a mut R> where F: Fn(&mut E) -> R;
    fn filter_mut<F>(&mut self, predicate: F) -> Vec<&mut E> where F: Fn(&&mut E) -> bool;
}

impl<E> VecExt<E> for Vec<E> {
    fn map<R, F>(self, mapping: F) -> Vec<R> where F: FnMut(E) -> R {
        self.into_iter().map(mapping).collect()
    }

    fn filter<F>(self, predicate: F) -> Vec<E> where F: Fn(&E) -> bool {
        self.into_iter().filter(predicate).collect()
    }

//    fn map_mut<'a, R, F>(&'a mut self, mapping: F) -> Vec<&'a mut R> where F: Fn(&mut E) -> R{
//        let map : Map<R,F> = self.iter_mut().map(mapping);
////        .collect::<Vec<&mut R>>()
//    }

    fn filter_mut<F>(& mut self, predicate: F) -> Vec<& mut E> where F: Fn(&&mut E) -> bool {
        self.iter_mut().filter(predicate).collect()
    }
}

/*
The difference between Path and PathBuf is roughly the same as the one between &str and String or &[] and Vec, ie.
 Path only holds a reference to the path string data but doesn't own this data, while PathBuf owns the string data itself.
  This means that a Path is immutable and can't be used longer than the actual data (held somewhere else) is available.

The reason why both types exists is to avoid allocations where possible,
 however, since most functions take both Path and PathBuf as arguments (by using AsRef<Path> for example),
  this usually doesn't have a big impact on your code.

A very rough guide for when to use Path vs. PathBuf:

For return types: if your function gets passed a Path[Buf] and returns a subpath of it,
 you can just return a Path (like Path[Buf].parent()), if you create a new path, or combine paths or anything like that,
  you need to return a PathBuf.

For arguments: Take a PathBuf if you need to store it somewhere, and a Path otherwise.

For arguments (advanced): In public interfaces, you usually don't want to use Path or PathBuf directly,
but rather a generic P: AsRef<Path> or P: Into<PathBuf>. That way the caller can pass in Path, PathBuf, &str or String.

As for your strip_prefix example: Calling to_str() on a Path[Buf] is very often a bad idea.
In fact, the reason why it returns an Option is that some paths simply aren't valid utf8 strings.

Then, once you implement proper error handling (in the most simple case just use the Error type from the failure crate),
 this example might just shrink to: path.strip_prefix(env::current_dir()?)? which looks more reasonable.
*/