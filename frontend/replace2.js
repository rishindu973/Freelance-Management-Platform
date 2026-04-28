const fs = require('fs');
const path = require('path');
const dir = path.resolve('d:/FF/Freelance-Management-Platform/frontend/src');

function getAllFiles(d) {
    let results = [];
    fs.readdirSync(d).forEach(file => {
        const full = path.join(d, file);
        if (fs.statSync(full).isDirectory()) results = results.concat(getAllFiles(full));
        else if (full.endsWith('.tsx') || full.endsWith('.ts')) results.push(full);
    });
    return results;
}

try {
    const files = getAllFiles(dir);
    let count = 0;
    files.forEach(filePath => {
        let content = fs.readFileSync(filePath, 'utf8');
        let original = content;

        content = content.replace(/\(\$\)/g, '(LKR)');
        content = content.replace(/\>\$/g, '>LKR ');
        content = content.replace(/\$\$\{/g, 'LKR ${');

        // Match `"$0` or `'$0` or `` `$0 ``
        content = content.replace(/([`"'])\$(\d)/g, '$1LKR $2');
        content = content.replace(/([`"'])\$([^`"'a-zA-Z\s])/g, '$1LKR $2');

        // Edge cases
        content = content.replace(/'\$'/g, "'LKR'");
        content = content.replace(/"\$"/g, '"LKR"');

        // Text like Amount ($)
        content = content.replace(/Amount \(\$\)/g, 'Amount (LKR)');
        content = content.replace(/Budget \(\$\)/g, 'Budget (LKR)');
        content = content.replace(/Paid \(\$\)/g, 'Paid (LKR)');

        if (content !== original) {
            fs.writeFileSync(filePath, content);
            count++;
        }
    });
    console.log('Modified files: ' + count);
} catch (e) {
    console.error(e);
}
