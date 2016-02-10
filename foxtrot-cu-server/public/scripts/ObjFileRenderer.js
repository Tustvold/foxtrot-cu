ObjFileRenderer = function(screen_width, screen_height, domElement) {
    // Will render the provided objData in a canvas of the specified dimensions added as a child of domElement
    var camera, scene, renderer, stats, controls;

    var model_renderer;
    var loader = new THREE.OBJLoader();

    init();
    animate();

    function init() {
        camera = new THREE.PerspectiveCamera(70, screen_width / screen_height, 1, 10000);
        camera.position.set(10, 10, 10);
        camera.lookAt(new THREE.Vector3(0, 0, 0));



        scene = new THREE.Scene();

        scene.add(new THREE.AmbientLight(0x555555));

        var light = new THREE.SpotLight(0xffffff);
        light.position.set(0, 500, 2000);
        scene.add(light);


        renderer = new THREE.WebGLRenderer({
            antialias: true
        });
        renderer.setClearColor(0xffffff);
        renderer.setPixelRatio(window.devicePixelRatio);
        renderer.setSize(screen_width, screen_height);
        renderer.sortObjects = false;

        domElement.appendChild(renderer.domElement);

        stats = new Stats();
        //stats.domElement.style.position = 'absolute';
        //stats.domElement.style.top = '0px';

        domElement.appendChild(stats.domElement);

        controls = new THREE.OrbitControls(camera, renderer.domElement);
        controls.mouseButtons = { ORBIT: THREE.MOUSE.MIDDLE, ZOOM: -1, PAN: -1 };
        controls.enableDamping = true;
        controls.dampingFactor = 0.25;
        controls.enableZoom = true;
        controls.enablePan = false;
        controls.enableKeys = false;
        controls.zoomSpeed = 3.0;

    }

    function animate() {

        requestAnimationFrame(animate);

        renderer.render(scene, camera);

        controls.update();

        stats.update();

    }

    this.setObjData = function(objData) {
        if (typeof model_renderer !== 'undefined')
            scene.remove(model_renderer)
        model_renderer = loader.parse(objData);
        scene.add(model_renderer)
    }

}
