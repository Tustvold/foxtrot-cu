var express = require('express');
var multer  = require('multer');
var exec = require('child_process').exec;
var router = express.Router();
var temp = require('temp');
var fs = require('fs');

var upload = multer({ dest: '/tmp/uploads/' });


router.post('/', upload.single('uploaded-mesh'), function(req,res,next){

    temp.track();

    temp.mkdir('outputDir', function(err, dirPath) {
        if (err)
            throw err;
        child = exec('java -jar "jars/EdibleLego-fat-1.0.jar" ' + req.file.path + " " + dirPath);
        console.log(req.file)
        console.log(dirPath);

        res.header("Content-Type", "application/json");

        hasError = false;
        errorBuffer = "";

        // Any error will return those errors
        child.stderr.on('data', function(data) {
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
                fs.readFile(dirPath+"/output.json", function(err, data) {
                    if (err)
                        throw err;
                    res.send(data);
                });
            }
        });
    });


});

module.exports = router;
