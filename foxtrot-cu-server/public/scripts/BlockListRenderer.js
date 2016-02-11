BlockListRenderer = function(screen_width, screen_height, domElement) {
    // Will create a canvas of the specified dimensions added as a child of domElement

    var blockList;
    var vertices, indices, colors, normals;
    var maxX, maxY = 0, maxZ = 0;
    var camera, scene, renderer, stats, controls;
    var model_renderer = null;

    var picking_material = new THREE.MeshBasicMaterial({
        vertexColors: THREE.VertexColors
    });

    var render_material = new THREE.MeshLambertMaterial({
        color: 0xff0000,
    });

    init();
    animate();

    function init() {
        camera = new THREE.PerspectiveCamera(70, screen_width / screen_height, 1, 10000);
        camera.position.set(10, 10, 10);
        camera.lookAt(new THREE.Vector3(0, 0, 0));



        scene = new THREE.Scene();

        scene.add(new THREE.AmbientLight(0x222222));

        var light = new THREE.DirectionalLight( 0xffffff, 0.7 );
        light.position.set(15,20,15);
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
        controls.mouseButtons = { ORBIT: THREE.MOUSE.MIDDLE, ZOOM: -1, PAN: -1 };
        controls.enableDamping = true;
        controls.dampingFactor = 0.25;
        controls.enableZoom = true;
        controls.enablePan = false;
        controls.enableKeys = false;
        controls.zoomSpeed = 3.0;

    }

    function generateMesh() {
        console.log("Started Mesh Generation");
        vertices = [], indices = [], colors = [], normals = [];

        var cur_index = 0;
        var block_id = -1;
        var color = new THREE.Color();

        maxX = blockList.length;
        if (maxX > 0)
            maxY = blockList[0].length;
        if (maxY > 0)
            maxZ = blockList[0][0].length;

        // The color ID space isn't sufficient for more blocks than this number
        if (maxX * maxY * maxZ > 16777214)
            throw new FatalError("Generated model has too many blocks");

        for (var x = 0; x < maxX; x++) {
            for (var y = 0; y < maxY; y++) {
                for (var z = 0; z < maxZ; z++) {
                    block_id++;
                    var block = blockList[x][y][z];
                    if (block == null)
                        continue;

                    if (x == maxX-1 || blockList[x+1][y][z] === null || blockList[x+1][y][z].use_custom_part) {
                        // Add positive x face
                        vertices.push(x+1, y, z);
                        vertices.push(x+1, y, z+1);
                        vertices.push(x+1, y+1, z);
                        vertices.push(x+1, y+1, z+1);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(1,0,0);
                        normals.push(1,0,0);
                        normals.push(1,0,0);
                        normals.push(1,0,0);

                        cur_index += 4;

                    }

                    if (x == 0 || blockList[x - 1][y][z] === null || blockList[x - 1][y][z].use_custom_part) {
                        // Add negative x face
                        vertices.push(x, y, z);
                        vertices.push(x, y+1, z);
                        vertices.push(x, y, z+1);
                        vertices.push(x, y+1, z+1);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(-1,0,0);
                        normals.push(-1,0,0);
                        normals.push(-1,0,0);
                        normals.push(-1,0,0);

                        cur_index += 4;
                    }

                    if (y == maxY -1 || blockList[x][y+1][z] === null || blockList[x][y+1][z].use_custom_part) {
                        // Add top face
                        vertices.push(x, y+1, z);
                        vertices.push(x+1, y+1, z);
                        vertices.push(x, y+1, z+1);
                        vertices.push(x+1, y+1, z+1);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(0,1,0);
                        normals.push(0,1,0);
                        normals.push(0,1,0);
                        normals.push(0,1,0);

                        cur_index += 4;
                    }

                    if (y == 0 || blockList[x][y - 1][z] === null || blockList[x][y - 1][z].use_custom_part) {
                        // Add bottom face
                        vertices.push(x, y, z);
                        vertices.push(x+1, y, z);
                        vertices.push(x, y, z+1);
                        vertices.push(x+1, y, z+1);

                        indices.push(cur_index, cur_index + 1, cur_index + 2);
                        indices.push(cur_index + 1, cur_index + 3, cur_index + 2);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(0,-1,0);
                        normals.push(0,-1,0);
                        normals.push(0,-1,0);
                        normals.push(0,-1,0);

                        cur_index += 4;
                    }

                    if (z == maxZ-1 || blockList[x][y][z + 1] === null || blockList[x][y][z + 1].use_custom_part) {
                        // Add positive z face
                        vertices.push(x, y, z+1);
                        vertices.push(x, y+1, z+1);
                        vertices.push(x+1, y, z+1);
                        vertices.push(x+1, y+1, z+1);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(0,0,1);
                        normals.push(0,0,1);
                        normals.push(0,0,1);
                        normals.push(0,0,1);

                        cur_index += 4;
                    }

                    if (z == 0 || blockList[x][y][z - 1] === null || blockList[x][y][z - 1].use_custom_part) {
                        // Add negative z face
                        vertices.push(x, y, z);
                        vertices.push(x+1, y, z);
                        vertices.push(x, y+1, z);
                        vertices.push(x+1, y+1, z);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(0,0,-1);
                        normals.push(0,0,-1);
                        normals.push(0,0,-1);
                        normals.push(0,0,-1);

                        cur_index += 4;
                    }
                }
            }
        }
        console.log("Generated Mesh");
    }

    function generateBuffer() {
        // This limit can be increased by using a Uint32Array instead
        if (indices.length >= 65536)
            throw new FatalError("Generated Mesh has too many indices");

        var floatVertices = new Float32Array(vertices);
        var intIndices = new Uint16Array(indices);
        var floatColors = new Float32Array(colors);
        var floatNormals = new Float32Array(normals);

        var geom = new THREE.BufferGeometry();
        geom.addAttribute( 'position', new THREE.BufferAttribute(floatVertices, 3));
        geom.addAttribute( 'normal', new THREE.BufferAttribute(floatNormals, 3));
        geom.addAttribute( 'color', new THREE.BufferAttribute(floatColors, 3));
        geom.addAttribute( 'index', new THREE.BufferAttribute(intIndices, 1));

        model_renderer = new THREE.Mesh(geom, render_material);
        scene.add(model_renderer);
    }

    this.setBlockList = function(blockList_) {
        blockList = blockList_;
        if (model_renderer != null)
            scene.remove(model_renderer);

        generateMesh();
        generateBuffer();
    }

    this.onBlockSelected = function(block) {
        console.log("Selected " + block);
    }

    function animate() {

        requestAnimationFrame(animate);

        renderer.render(scene, camera);

        controls.update();

        stats.update();

    }
}
