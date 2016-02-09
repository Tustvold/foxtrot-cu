var express = require('express');
var multer  = require('multer');
var exec = require('child_process').exec;
var router = express.Router();

var upload = multer({ dest: '/tmp/uploads/' });


router.post('/', upload.single('uploaded-mesh'), function(req,res,next){

    child = exec('<run exec> ' + req.file.path);
    console.log(req.file)

    // Any data from the jar
    child.stdout.on('data', function(data) {
        if(!res.finished) {
            res.write(data);
        }
    });

    // Any error will return those errors
    child.stderr.on('data', function(data) {
        console.log(data)
        res.status(200).send({ error: data }).end()
    });

    // If the jar finished successfully
    child.on('exit', function (code) {
        if(!res.finished) {
            setTimeout(function () { // to simulate a long running jar file.
                res.end();
                console.log('child process exited with code ' + code);
            }, 5 * 1000)
        }
    });

});

module.exports = router;
