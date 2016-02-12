ObjFileRenderer = function(screen_width, screen_height, domElement) {
    // Will render the provided objData in a canvas of the specified dimensions added as a child of domElement
    var camera, scene, pickingScene, renderer, stats, controls;

    var model_renderer;
    var loader = new THREE.OBJLoader();

    init();
    animate();

    function init() {
        camera = new THREE.PerspectiveCamera(70, screen_width / screen_height, 1, 10000);
        camera.position.set(10, 10, 10);
        camera.lookAt(new THREE.Vector3(0, 0, 0));

        pickingScene = new THREE.Scene();

        scene = new THREE.Scene();

        scene.add(new THREE.AmbientLight(0x555555));

        var light = new THREE.DirectionalLight( 0xffffff, 0.5 );
        light.position.set(1500,2000,1500);
        light.target.position.set(0,0,0);
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
        controls.mouseButtons = { ORBIT: THREE.MOUSE.RIGHT, ZOOM: -1, PAN: -1 };
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
        var bounds = {min: new THREE.Vector3(Number.MAX_VALUE, Number.MAX_VALUE, Number.MAX_VALUE), max: new THREE.Vector3(Number.MIN_VALUE, Number.MIN_VALUE, Number.MIN_VALUE)};
        model_renderer = loader.parse(objData, bounds);

        var center = new THREE.Vector3((bounds.min.x + bounds.max.x) / 2.0, (bounds.min.y + bounds.max.y) / 2.0, (bounds.min.z + bounds.max.z) / 2.0);
        var half_extents = new THREE.Vector3((bounds.max.x - bounds.min.x) / 2.0, (bounds.max.y - bounds.min.y) / 2.0, (bounds.max.z - bounds.min.z) / 2.0);

        var radius = half_extents.length() + 10.0;

        controls.position0.addVectors(center, new THREE.Vector3(radius,radius,radius));
        controls.target0 = center;
        controls.reset();

        scene.add(model_renderer)
    }

}
