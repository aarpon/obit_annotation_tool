#include <iostream>
#include <cstdlib>

int main(int argc, char **argv)
{
    // Check if the processor is available
    if (!system(NULL))
        exit(1);
    
    // Launch the Java virtual machine with enough memory and run 
    // SCUOpenBISImporterAdmin.
	return system("java -jar ./lib/SCUOpenBISImporterAdmin.jar");
}

