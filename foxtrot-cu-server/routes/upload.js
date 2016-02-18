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
        console.log(req.file)
        console.log(tempfile.path);
        var args = 'java -jar "jars/EdibleLego-fat-1.0.jar" voxelise ' + req.file.path + " " + tempfile.path + " " + "3" + " " + "1.0"
        console.log(args);
        child = exec(args);

        res.header("Content-Type", "application/json");

        hasError = false;
        errorBuffer = "";

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
        child.on('exit', function (code) {
            if (hasError) {
                console.log(errorBuffer);
                res.status(200).json({error: errorBuffer});
                res.end();
            }
            else if(!res.finished) {
                fs.readFile(tempfile.path, function(err, data) {
                    if (err)
                        throw err;
                    res.send(data);
                });
            }
        });
    })
});



module.exports = router;
