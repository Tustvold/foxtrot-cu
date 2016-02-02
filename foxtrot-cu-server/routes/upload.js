var express = require('express');
var multer  = require('multer');
var exec = require('child_process').exec;
var router = express.Router();

var upload = multer({ dest: '/tmp/uploads/' });


router.post('/', upload.any(), function(req,res,next){
    console.log(req.body);
    console.log(req.files);

    child = exec('java -jar jar/final.jar 2 2');
    child.stdout.on('data', function(data) {
        res.write(data);
    });
    child.on('exit', function (code) {
        res.end(code.toString());
        console.log('child process exited with code ' + code);
    });
});

module.exports = router;
