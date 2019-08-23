const { gitDescribeSync } = require('git-describe');
const { writeFileSync } = require('fs');
const path = require('path');
const info = gitDescribeSync();
const infoJson = JSON.stringify(info, null, 2);
writeFileSync(path.join(__dirname, '/src/app/about/git-version.json'), infoJson);
