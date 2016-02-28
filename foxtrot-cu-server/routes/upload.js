var express = require('express');
var multer  = require('multer');
var exec = require('child_process').exec;
var router = express.Router();
var temp = require('temp');
var fs = require('fs');

var upload = multer({ dest: '/tmp/uploads/' });


router.post('/', upload.single('uploaded-mesh'), function(req,res,next){

    temp.track();

    temp.open("outputjson", function (err, tempfile) {
        if (err)
            throw err;
        //console.log(req.file)
        var block_size = 10;
        var block_number = 10;
        if (typeof req.body.block_size !== 'undefined') {
            block_size = req.body.block_size
        }
        if (typeof req.body.block_number !== 'undefined') {
            block_number = req.body.block_number
        }

        var args = 'java -jar "jars/EdibleLego-fat-1.0.jar" voxelise ' + req.file.path + " " + tempfile.path + " " + block_number + " " + block_size
        console.log(args);
        hasError = false;
        errorBuffer = "";

        child = exec(args, {timeout: 1000*60});

        res.header("Content-Type", "application/json");

        child.stdout.on('data', function(data) {
            //console.log(data);
        })

        // Any error will return those errors
        child.stderr.on('data', function(data) {
            hasError = true;
            errorBuffer += data;
            console.log(data)
        });

        // If the jar finished successfully
        child.on('exit', function (code, signal) {
            if (signal) {
                console.log("Terminated with signal: " + signal);
                if (signal === "SIGTERM")
                    res.status(200).json({error: "Process Timed Out"}); // We assume it timed out
                else
                    res.status(200).json({error: "Process Exited With Signal: " + signal});
                res.end();
            }
            else if (hasError) {
                console.log(errorBuffer);
                res.status(200).json({error: errorBuffer});
                res.end();
            }
            else if(!res.finished) {
                fs.readFile(tempfile.path, function(err, data) {
                    if (err)
                        throw err;
                    res.send(data);
                    console.log(tempfile.path);
                    fs.unlink(tempfile.path)
                });
            }

        });
    })
});



module.exports = router;
