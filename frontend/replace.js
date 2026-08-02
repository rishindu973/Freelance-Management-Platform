const fs = require('fs');
const path = require('path');

function replaceDollarsInFile(filePath) {
    if (!fs.existsSync(filePath)) return;
    let content = fs.readFileSync(filePath, 'utf8');
    let original = content;

    // React JSX text
    content = content.replace(/\>\$/g, '>LKR ');
    // Handle specific static formatting
    content = content.replace(/\(\$\)/g, '(LKR)');

    // JS Template literals: `$${...}` --> `LKR ${...}`
    content = content.replace(/\$\$\{/g, 'LKR ${');
    // JS general strings
    content = content.replace(/`\$/g, '`LKR ');
    content = content.replace(/"\$/g, '"LKR ');
    content = content.replace(/'\$/g, "'LKR ");

    if (content !== original) {
        fs.writeFileSync(filePath, content);
    }
}

const dir = 'd:/FF/Freelance-Management-Platform/frontend/src';
const getAllFiles = (d) => {
    let results = [];
    fs.readdirSync(d).forEach(file => {
        let full = path.join(d, file);
        if (fs.statSync(full).isDirectory()) results = results.concat(getAllFiles(full));
        else if (full.endsWith('.tsx') || full.endsWith('.ts')) results.push(full);
    });
    return results;
};

getAllFiles(dir).forEach(replaceDollarsInFile);
console.log('Done');
