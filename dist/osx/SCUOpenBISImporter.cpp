#include <iostream>
#include <cstdlib>

int main(int argc, char **argv)
{
    // Check if the processor is available
    if (!system(NULL))
        exit(1);
    
    // Launch the Java virtual machine with enough memory and run 
    // SCUOpenBISImporter.
	return system("java -Xms512m -Xmx2048m -XX:MaxPermSize=512m -jar ./lib/SCUOpenBISImporter.jar");
}

