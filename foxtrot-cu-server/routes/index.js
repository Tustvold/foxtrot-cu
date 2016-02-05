var express = require('express');
var multer  = require('multer')
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

router.get('/upload-form',function(req, res, next) {
  res.render('upload-form');
});

router.get('/test-cubes',function(req, res, next) {
  res.render('test-cubes');
});

module.exports = router;
