const http = require('http');

const postData = JSON.stringify({ "email": "test_node_final@test.com", "fullName": "Test Node", "title": "QA", "contactNumber": "123", "driveLink": "http://node", "role": "FREELANCER", "status": "Active" });

const options = {
    hostname: 'localhost',
    port: 8081,
    path: '/api/freelancer/create',
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(postData)
    }
};

const req = http.request(options, (res) => {
    let rawData = '';
    res.on('data', (chunk) => { rawData += chunk; });
    res.on('end', () => {
        console.log("POST STATUS:", res.statusCode);
        console.log("POST BODY:", rawData);
    });
});
req.on('error', (e) => console.error(e));
req.write(postData);
req.end();

const req2 = http.request({ hostname: 'localhost', port: 8081, path: '/api/manager/profile', method: 'GET' }, res => {
    let rawData = '';
    res.on('data', chunk => rawData += chunk);
    res.on('end', () => console.log("GET MANAGER:", res.statusCode, rawData));
});
req2.on('error', e => console.error(e));
req2.end();
