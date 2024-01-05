#!/bin/bash

filePath="esthesis-core/esthesis-core-docs/src/css/custom.css"
cssCode=".dropdown__menu > li:nth-child(1) { display: none\!important; }"
sed -i "\$a\\$cssCode" "$filePath"