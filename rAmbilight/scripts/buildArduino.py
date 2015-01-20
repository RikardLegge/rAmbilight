#!/usr/bin/python
import re

src_file_path = "../src/com/rambilight/core/clientInterface/debug/ArduinoEmulator.java"
target_file_path = "../arduino/driver-dev/driver-dev.ino"

def readfile(file_path):
	filehandler = open(file_path, "r")
	filecontents = filehandler.read()
	filehandler.close()
	return filecontents

def writefile(file_path, file_contents):
	file_handler = open(file_path, "w")
	file_handler.write(file_contents)
	file_handler.close()

src_parsing = readfile(src_file_path)

# Replace java with arduino clauses.
src_parsing = re.sub( r'(?:\/\* java [\s\S]*? \/java [\S]*)','', src_parsing, flags=re.M|re.I)
src_parsing = re.sub( r'(\/\* arduino \/)|(\/\* \/arduino \*\/)','', src_parsing, flags=re.M|re.I)

# Replace types.
src_parsing = re.sub( r'final int ([\S]*)[\s]*=[\s]*([\S]*);',r'#define \1 \2', src_parsing, flags=re.M|re.I)
src_parsing = re.sub( r'int\/\*\*\/','byte', src_parsing, flags=re.M|re.I)
src_parsing = re.sub( r'double','float', src_parsing, flags=re.M|re.I)
src_parsing = re.sub( r'long','unsigned long', src_parsing, flags=re.M|re.I)
src_parsing = re.sub( r'Math\.','', src_parsing, flags=re.M|re.I)

# Format the code.
src_parsing = re.sub( r'^[ ]{4}','', src_parsing, flags=re.M|re.I)

writefile(target_file_path, src_parsing)
#print src_parsing
print 'file compiled sucessfully!'