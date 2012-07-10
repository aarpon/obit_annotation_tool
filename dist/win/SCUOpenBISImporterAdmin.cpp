#include "stdafx.h"
#include <iostream>
#include <cstdlib>
#include <Windows.h>

// Show/Hide the console applications console window
void ShowConsoleWindow(bool bHide)
{   
        ShowWindow(GetConsoleWindow(), (bHide) ? SW_SHOW : SW_HIDE);
}

int _tmain(int argc, _TCHAR* argv[])
{
    // Hide console
    ShowConsoleWindow(false);

    // Check if the processor is available
    if (!system(NULL))
        exit(1);
    
    // Launch the Java virtual machine with enough memory and run 
    // SCUOpenBISImporter.
    return system("java -jar ./lib/SCUOpenBISImporterAdmin.jar");
}
