var express = require('express');
var archiver = require('archiver');
var exec = require('child_process').exec;
var router = express.Router();
var temp = require('temp');
var fs = require('fs');



router.post('/', function(req,res,next){

    temp.track();
    var archive = archiver('zip')

    archive.on('error', function(err){
        throw err;
    });

    // close the stream nicely.
    archive.on('end', function(err) {
        if(!res.finished) {
            res.end();
        }
        console.log(archive.pointer() + ' total bytes');
        console.log('archiver has been finalized and the output file descriptor has closed.');
    });


    temp.mkdir('outputDir', function(err, dirPath) {
        if (err)
            throw err;
        console.log(req)
        //var body =
        console.log(req.body)
        console.log(req.body.block_list)
        var filePath = dirPath + '/in.json'
        fs.writeFile(filePath, function(err) {
            if (err) {
                return console.log(err)
            }
            child = exec('java -jar "jars/EdibleLego-fat-1.0.jar" mouldify' + filePath + " " + dirPath);


            archive.pipe(res);
            archive.bulk([
                { expand: true, cwd: dirPath, src: ['*.stl'], dest: 'parts'}
            ]);

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
                    res.header("Content-Type", "application/json");
                    console.log(errorBuffer);
                    res.status(200).json({error: errorBuffer});
                    res.end();
                }
                else if(!res.finished) {
                    res.header("Content-Type", "application/octet-stream");
                    archive.finalize();
                }
            });
        })
    });
});



module.exports = router;
