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

        var filePath = dirPath + '/in.json'
        fs.writeFile(filePath, JSON.stringify(req.body.block_list), function(err) {
            if (err) {
                return console.log(err)
            }

            var custom_part_size = 10;
            if (typeof req.body.custom_part_size !== 'undefined') {
                custom_part_size = req.body.custom_part_size
            }

            var exec_str = 'java -jar "jars/EdibleLego-fat-1.0.jar" mouldify ' + filePath + " " + dirPath + " " + custom_part_size;
            console.log(exec_str)
            child = exec(exec_str);

            hasError = false;
            errorBuffer = "";

            // Any error will return those errors
            child.stderr.on('data', function(data) {
                errorBuffer += data;
                console.log(data)
                hasError = true;
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
                    archive.pipe(res);
                    archive.bulk([
                        { expand: true, cwd: dirPath, src: ['*.obj'], dest: 'parts'}
                    ]);

                    res.header("Content-Type", "application/octet-stream");
                    archive.finalize();
                }
            });
        })
    });
});



module.exports = router;
