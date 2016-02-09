var express = require('express');
var multer  = require('multer');
var exec = require('child_process').exec;
var router = express.Router();

var upload = multer({ dest: '/tmp/uploads/' });


router.post('/', upload.any(), function(req,res,next){

    child = exec('java -jar jar/backend.jar ' + req.files[0].path);
    console.log('here')
    console.log(child)
    child.stdout.on('data', function(data) {
        res.write(data);
    });
    child.stderr.on('data', function(data) {
        console.log(data)
    })
    child.on('exit', function (code) {
        setTimeout(function() { // to simulate a long running jar file.
            res.end(code.toString());
            console.log('child process exited with code ' + code);
        }, 5 * 1000)
    });
});

module.exports = router;
