package cam.ac.uk.foxtrot.instructions;


public abstract class InstructionGenerator {

    /**
     * Generates the instructions in the provided directory
     *
     * A file called root.html will be generated in the provided directory
     * which will be the root level of the instructions
     *
     * @param directory the directory to create the instructions in
     */
    public abstract void generateInstructions(String directory);
}
