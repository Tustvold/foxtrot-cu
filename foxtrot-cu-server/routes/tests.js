var express = require('express');
var archiver = require('archiver');
var exec = require('child_process').exec;
var router = express.Router();
var temp = require('temp');
var fs = require('fs');


router.get('/zipper', function(req,res,next){

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
        console.log(dirPath)
        fs.writeFileSync(dirPath + '/1.stl', 'hello.txt');
        fs.writeFileSync(dirPath + '/2.stl', 'hello2.txt');
        fs.writeFileSync(dirPath + '/3.stl', 'hello3.txt');
        fs.writeFileSync(dirPath + '/4.txt', 'hello4.txt');



        archive.pipe(res);
        archive.bulk([
            { expand: true, cwd: dirPath, src: ['*.stl'], dest: 'parts'}
        ]);

        res.header("Content-Type", "application/octet-stream");
        archive.finalize();



    });
});



module.exports = router;
