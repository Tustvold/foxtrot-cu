BlockListRenderer = function(screen_width, screen_height, domElement) {
    // Will create a canvas of the specified dimensions added as a child of domElement

    var blockList;
    var vertices, indices, colors, normals;
    var maxX, maxY = 0,
        maxZ = 0, yCap = 0;
    var camera, scene, renderer, stats, controls;
    var model_renderer = null;
    var model_picker = null;
    var gridXZ = null;
    var mouse = new THREE.Vector2();

    var pickingScene, pickingTexture;

    var picking_material = new THREE.MeshBasicMaterial({
        vertexColors: THREE.VertexColors
    });

    var render_material = new THREE.MeshLambertMaterial({
        color: 0xff0000,
    });

    var render_material_wireframe = new THREE.MeshBasicMaterial({
        color: 0x222222, wireframe: true
    });

    var scope = this;

    init();
    animate();

    function init() {
        camera = new THREE.PerspectiveCamera(70, screen_width / screen_height, 1, 10000);
        camera.position.set(10, 10, 10);
        camera.lookAt(new THREE.Vector3(0, 0, 0));

        scene = new THREE.Scene();
        scene.add(new THREE.AmbientLight(0x555555));

        var light = new THREE.DirectionalLight(0xffffff, 0.5);
        light.position.set(1500, 2000, 1500);
        light.target.position.set(0, 0, 0);
        scene.add(light);

        pickingScene = new THREE.Scene();
        pickingTexture = new THREE.WebGLRenderTarget(screen_width, screen_height);
        pickingTexture.minFilter = THREE.LinearFilter;
        pickingTexture.generateMipmaps = false;

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
        controls.mouseButtons = {
            ORBIT: THREE.MOUSE.RIGHT,
            ZOOM: -1,
            PAN: -1
        };
        controls.enableDamping = true;
        controls.dampingFactor = 0.25;
        controls.enableZoom = true;
        controls.enablePan = false;
        controls.enableKeys = false;
        controls.zoomSpeed = 3.0;

        renderer.domElement.addEventListener('mousedown', onMouseDown);
        renderer.domElement.addEventListener('mousemove', onMouseMove);
        // For some reason renderer.domElement doesn't work here...
        // TODO: Determine why
        document.addEventListener('keydown', onKeyDown);
    }

    function generateMesh() {
        //console.log("Started Mesh Generation");
        vertices = [], indices = [], colors = [], normals = [];

        var cur_index = 0;
        var block_id = -1;
        var color = new THREE.Color();

        // The color ID space isn't sufficient for more blocks than this number
        if (maxX * maxY * maxZ > 16777214)
            throw new FatalError("Generated model has too many blocks");

        for (var y = 0; y < Math.min(yCap, maxY); y++) {
            for (var x = 0; x < maxX; x++) {
                for (var z = 0; z < maxZ; z++) {
                    block_id++;
                    var block = blockList[x][y][z];
                    if (block == null)
                        continue;
                    color.setHex(block_id);

                    if (x == maxX - 1 || blockList[x + 1][y][z] === null || blockList[x + 1][y][z].use_custom_part) {
                        // Add positive x face
                        vertices.push(x + 1, y, z);
                        vertices.push(x + 1, y, z + 1);
                        vertices.push(x + 1, y + 1, z);
                        vertices.push(x + 1, y + 1, z + 1);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(1, 0, 0);
                        normals.push(1, 0, 0);
                        normals.push(1, 0, 0);
                        normals.push(1, 0, 0);

                        cur_index += 4;

                    }

                    if (x == 0 || blockList[x - 1][y][z] === null || blockList[x - 1][y][z].use_custom_part) {
                        // Add negative x face
                        vertices.push(x, y, z);
                        vertices.push(x, y + 1, z);
                        vertices.push(x, y, z + 1);
                        vertices.push(x, y + 1, z + 1);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(-1, 0, 0);
                        normals.push(-1, 0, 0);
                        normals.push(-1, 0, 0);
                        normals.push(-1, 0, 0);

                        cur_index += 4;
                    }

                    if (y == yCap - 1 || blockList[x][y + 1][z] === null || blockList[x][y + 1][z].use_custom_part) {
                        // Add top face
                        vertices.push(x, y + 1, z);
                        vertices.push(x + 1, y + 1, z);
                        vertices.push(x, y + 1, z + 1);
                        vertices.push(x + 1, y + 1, z + 1);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(0, 1, 0);
                        normals.push(0, 1, 0);
                        normals.push(0, 1, 0);
                        normals.push(0, 1, 0);

                        cur_index += 4;
                    }

                    if (y == 0 || blockList[x][y - 1][z] === null || blockList[x][y - 1][z].use_custom_part) {
                        // Add bottom face
                        vertices.push(x, y, z);
                        vertices.push(x + 1, y, z);
                        vertices.push(x, y, z + 1);
                        vertices.push(x + 1, y, z + 1);

                        indices.push(cur_index, cur_index + 1, cur_index + 2);
                        indices.push(cur_index + 1, cur_index + 3, cur_index + 2);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(0, -1, 0);
                        normals.push(0, -1, 0);
                        normals.push(0, -1, 0);
                        normals.push(0, -1, 0);

                        cur_index += 4;
                    }

                    if (z == maxZ - 1 || blockList[x][y][z + 1] === null || blockList[x][y][z + 1].use_custom_part) {
                        // Add positive z face
                        vertices.push(x, y, z + 1);
                        vertices.push(x, y + 1, z + 1);
                        vertices.push(x + 1, y, z + 1);
                        vertices.push(x + 1, y + 1, z + 1);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(0, 0, 1);
                        normals.push(0, 0, 1);
                        normals.push(0, 0, 1);
                        normals.push(0, 0, 1);

                        cur_index += 4;
                    }

                    if (z == 0 || blockList[x][y][z - 1] === null || blockList[x][y][z - 1].use_custom_part) {
                        // Add negative z face
                        vertices.push(x, y, z);
                        vertices.push(x + 1, y, z);
                        vertices.push(x, y + 1, z);
                        vertices.push(x + 1, y + 1, z);

                        indices.push(cur_index, cur_index + 2, cur_index + 1);
                        indices.push(cur_index + 1, cur_index + 2, cur_index + 3);

                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);
                        colors.push(color.r, color.g, color.b);

                        normals.push(0, 0, -1);
                        normals.push(0, 0, -1);
                        normals.push(0, 0, -1);
                        normals.push(0, 0, -1);

                        cur_index += 4;
                    }
                }
            }
        }
        //console.log("Generated Mesh");
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
        geom.addAttribute('position', new THREE.BufferAttribute(floatVertices, 3));
        geom.addAttribute('normal', new THREE.BufferAttribute(floatNormals, 3));
        geom.addAttribute('color', new THREE.BufferAttribute(floatColors, 3));
        geom.setIndex(new THREE.BufferAttribute(intIndices, 1));

        model_renderer = new THREE.Mesh(geom, render_material);

        //model_renderer = THREE.SceneUtils.createMultiMaterialObject(geom, [
        //    render_material,
        //    render_material_wireframe
        //]);

        new THREE.Mesh(geom, picking_material);

        scene.add(model_renderer);

        model_picker = new THREE.Mesh(geom, picking_material);
        pickingScene.add(model_picker);
    }

    this.setBlockList = function(blockList_) {
        blockList = blockList_;

        maxX = blockList.length;
        if (maxX > 0)
            maxY = blockList[0].length;
        if (maxY > 0)
            maxZ = blockList[0][0].length;
        yCap = maxY;

        this.refresh();

        var center = new THREE.Vector3(-maxX / 2, -maxY / 2, -maxZ / 2);
        var radius = center.length() + 10.0;
        controls.position0 = new THREE.Vector3(radius, radius, radius);
        controls.reset();
    }

    this.refresh = function() {
        if (model_renderer != null)
            scene.remove(model_renderer);
        if (model_picker != null)
            pickingScene.remove(model_picker);
        if (gridXZ != null)
            scene.remove(gridXZ);

        generateMesh();
        generateBuffer();



        model_renderer.position.set(-maxX / 2, 0, -maxZ / 2);
        model_picker.position.set(-maxX / 2, 0, -maxZ / 2);

        var gridSize = Math.floor(Math.max(maxX,maxZ)/2) + 2

        gridXZ = new THREE.GridHelper(gridSize, 1);
        scene.add(gridXZ);
    }

    this.onBlockSelected = function(x, y, z, block) {
        console.log("Selected Block at X: " + x + " Y: " + y + " Z: " + z);
    }

    this.setYRenderCap = function(newYCap) {
        if (newYCap > maxY || newYCap < 1)
            return;
        yCap = newYCap;
        this.refresh();
    }

    this.getYRenderCap = function() {
        return yCap;
    }

    function pick() {

        //render the picking scene off-screen

        renderer.render(pickingScene, camera, pickingTexture);

        //create buffer for reading single pixel
        var pixelBuffer = new Uint8Array(4);

        var rect = renderer.domElement.getBoundingClientRect();

        //read the pixel under the mouse from the texture
        renderer.readRenderTargetPixels(pickingTexture, mouse.x - rect.left, pickingTexture.height - (mouse.y - rect.top), 1, 1, pixelBuffer);

        //interpret the pixel as an ID
        var id = (pixelBuffer[0] << 16) | (pixelBuffer[1] << 8) | (pixelBuffer[2]);

        // This will need to be changed if the clear color is changed
        if (id != 0xFFFFFF) {
            var y = Math.floor((id % (maxX *maxZ * maxY)) / maxZ / maxX);
            var x = Math.floor(id % (maxX * maxZ) / maxZ);
            var z = id % maxZ;



            if (x >= 0 && x < maxX && y >= 0 && y < maxY && z >= 0 && z < maxZ) {
                if (typeof blockList[x][y][z] === "undefined")
                    console.log("Error: Picking selected non-existent block");
                else
                    scope.onBlockSelected(x, y, z, blockList[x][y][z]);
            }

        }


    }

    function animate() {

        requestAnimationFrame(animate);

        renderer.render(scene, camera);

        controls.update();

        stats.update();

    }

    function onMouseDown(event) {
        if (event.button == THREE.MOUSE.LEFT)
            pick();
    }

    function onMouseMove(e) {
        mouse.x = e.clientX;
        mouse.y = e.clientY;
    }

    function onKeyDown(e) {
        if (e.keyCode == 37) {
            // Left Arrow
            e.preventDefault();
        } else if (e.keyCode == 38) {
            // Up Arrow
            scope.setYRenderCap(scope.getYRenderCap() + 1);
            e.preventDefault();
        } else if (e.keyCode == 39) {
            // Right Arrow
            e.preventDefault();
        } else if (e.keyCode == 40) {
            // Down Arrow
            scope.setYRenderCap(scope.getYRenderCap() - 1);
            e.preventDefault();
        }
    }
}
