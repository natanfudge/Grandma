import util.className

class RenameTests {
    fun `Rename unnamed class`(){
        val rename = className("TestUnnamed").renamedTo("TestNamed")
        //TODO: Place mock files in resources folder, at the start copy to wanted location and then compare
        // to another mock file that is the expected outcome.
    }
}