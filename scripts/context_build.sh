#!/bin/bash

# Create a directory for the output
mkdir -p project_context

# Get Maven project structure
echo "Collecting Maven project structure..."
mvn help:effective-pom -Doutput=project_context/effective-pom.xml

# Get Maven dependencies
echo "Collecting Maven dependencies..."
mvn dependency:tree > project_context/dependencies.txt

# Function to extract package, class, method and field info from Java files
extract_java_info() {
  local file=$1
  local output=$2

  echo "FILE: $file" >> "$output"

  # Extract package
  grep "^package" "$file" >> "$output"

  # Extract imports
  grep "^import" "$file" >> "$output"

  # Extract class/interface declarations
  grep -E "^[[:space:]]*(public|private|protected)[[:space:]]*(abstract|final)?[[:space:]]*(class|interface|enum)" "$file" >> "$output"

  # Extract method signatures
  grep -E "^[[:space:]]*(public|private|protected)[[:space:]]*(static|final|abstract|synchronized)?[[:space:]]*[A-Za-z0-9_<>]+[[:space:]]+[A-Za-z0-9_]+[[:space:]]*\([^)]*\)" "$file" >> "$output"

  # Extract field declarations
  grep -E "^[[:space:]]*(public|private|protected)[[:space:]]*(static|final)?[[:space:]]*[A-Za-z0-9_<>]+[[:space:]]+[A-Za-z0-9_]+" "$file" | grep -v "(" >> "$output"

  echo -e "\n\n" >> "$output"
}

# Collect module information
echo "Collecting module information..."
find . -name "pom.xml" | sort > project_context/modules.txt

# Get directory structure
echo "Collecting directory structure..."
find . -type d | grep -v "target\|\.git\|\.idea" | sort > project_context/directories.txt

# Collect all Java files
echo "Finding all Java files..."
find . -name "*.java" | grep -v "target" | sort > project_context/java_files.txt

# Extract information from all Java files
echo "Extracting Java code information..."
touch project_context/java_code_info.txt
while IFS= read -r java_file; do
  echo "Processing $java_file"
  extract_java_info "$java_file" "project_context/java_code_info.txt"
done < project_context/java_files.txt

# Collect Spring configuration if present
echo "Collecting configuration files..."
find . -name "application*.properties" -o -name "application*.yml" | xargs cat > project_context/spring_config.txt 2>/dev/null

# Create a zip file with all the context
echo "Creating final zip file..."
cd project_context
zip -r ../project_context.zip ./*
cd ..

echo "Done! Project context has been saved to project_context.zip"