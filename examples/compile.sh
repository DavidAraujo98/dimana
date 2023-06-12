#!/bin/bash

# Output file name
export OUT_FILE=${1%.*}

# Export OUT_FILE to a file
echo "$OUT_FILE" > lastCompilation.txt

# Travel to src/
cd ../src

# Output file back to examples with same name has source file
java GrammarMain -o $OUT_FILE.java ../examples/$1
javac -d ../examples $OUT_FILE.java


# Clean temporary files
rm $OUT_FILE.java

# Travel back
cd ../examples