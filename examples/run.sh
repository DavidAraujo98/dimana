#!/bin/bash

clear 

# Read the OUT_FILE
OUT_FILE=$(cat lastCompilation.txt)

# Remove OUT_FILE
rm lastCompilation.txt

echo -e "$OUT_FILE.class created!\n"

# Run examples
java $OUT_FILE