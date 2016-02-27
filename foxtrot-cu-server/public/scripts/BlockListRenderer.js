BlockListRenderer = function(screen_width, screen_height, domElement) {
    // Will create a canvas of the specified dimensions added as a child of domElement

    var blockList;
    var vertices, indices, colors;
    var maxX, maxY = 0,
        maxZ = 0,
        yCap = 0;
    var camera, scene, renderer, stats, controls;
    var selectBox;
    var model_renderer = null;
    var model_picker = null;
    var gridXZ = null;
    var mouse = new THREE.Vector2();

    var pickingScene, pickingTexture;

    var picking_material = new THREE.MeshBasicMaterial({
        vertexColors: THREE.VertexColors,
        side: THREE.DoubleSide
    });

    var render_material = new THREE.MeshLambertMaterial({
        color: 0xFF0000,
        side: THREE.DoubleSide
    });

    var render_material_wireframe = new THREE.MeshBasicMaterial({
        color: 0x222222,
        wireframe: true
    });


    var select_box_material = new THREE.MeshBasicMaterial({
        color: 0xFFE135,
        wireframe: true
    });

    var selectBoxDim = 1.02;
    var selectBoxHalfDim = selectBoxDim / 2.0;

    var scope = this;

    var maxBlockID = 0;
    var yLimitEnabled = false;
    var idLimitEnabled = false;
    var hoverDetectionEnabled = false;

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

        var selectBoxGeom = new THREE.BoxGeometry(selectBoxDim, selectBoxDim, selectBoxDim);

		selectBox = new THREE.Mesh( selectBoxGeom, select_box_material );
        selectBox.visible = false;
        scene.add(selectBox);

        domElement.appendChild(renderer.domElement);

        //stats = new Stats();
        //stats.domElement.style.position = 'absolute';
        //stats.domElement.style.top = '0px';

        //domElement.appendChild(stats.domElement);

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
        vertices = [], indices = [], colors = [];

        var cur_index = 0;
        var color = new THREE.Color();

        // The color ID space isn't sufficient for more blocks than this number
        if (maxX * maxY * maxZ > 16777214)
            throw new FatalError("Generated model has too many blocks");

        var yLimit = maxY;
        if (yLimitEnabled)
            yLimit = Math.min(yLimit, yCap);

        for (var y = 0; y < yLimit; y++) {
            for (var x = 0; x < maxX; x++) {
                for (var z = 0; z < maxZ; z++) {
                    var block = blockList[x][y][z];
                    if (block == null)
                        continue;
                    var block_id = getBlockID(x, y, z);
                    if (idLimitEnabled && block_id > maxBlockID)
                        return;
                    color.setHex(block_id);

                    if (block.use_custom_part) {
                        var custom_part = block.custom_part_array[block.custom_part_index];
                        var triangle_array = custom_part.triangle_array;

                        for (var i = 0; i < triangle_array.length; i++) {
                            var vertex = triangle_array[i];
                            vertices.push(vertex.x + x, vertex.y + y, vertex.z + z);
                            indices.push(cur_index);
                            colors.push(color.r, color.g, color.b);
                            cur_index++;
                        }

                        continue;
                    }

                    if (x == maxX - 1 || blockList[x + 1][y][z] === null || blockList[x + 1][y][z].use_custom_part || getBlockID(x + 1, y, z) > maxBlockID) {
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

                        cur_index += 4;

                    }

                    if (x == 0 || blockList[x - 1][y][z] === null || blockList[x - 1][y][z].use_custom_part || getBlockID(x - 1, y, z) > maxBlockID) {
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

                        cur_index += 4;
                    }

                    if (y == yCap - 1 || blockList[x][y + 1][z] === null || blockList[x][y + 1][z].use_custom_part || getBlockID(x, y + 1, z) > maxBlockID) {
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

                        cur_index += 4;
                    }

                    if (y == 0 || blockList[x][y - 1][z] === null || blockList[x][y - 1][z].use_custom_part || getBlockID(x, y - 1, z) > maxBlockID) {
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

                        cur_index += 4;
                    }

                    if (z == maxZ - 1 || blockList[x][y][z + 1] === null || blockList[x][y][z + 1].use_custom_part || getBlockID(x, y, z + 1) > maxBlockID) {
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

                        cur_index += 4;
                    }

                    if (z == 0 || blockList[x][y][z - 1] === null || blockList[x][y][z - 1].use_custom_part || getBlockID(x, y, z - 1) > maxBlockID) {
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

                        cur_index += 4;
                    }
                }
            }
        }
        //console.log("Generated Mesh");
    }

    function generateBuffer() {
        // This limit can be increased by using a Uint32Array instead
        if (indices.length >= 4294967296)
            throw new FatalError("Generated Mesh has too many indices");

        var floatVertices = new Float32Array(vertices);
        var intIndices = new Uint32Array(indices);
        var floatColors = new Float32Array(colors);

        var geom = new THREE.BufferGeometry();
        geom.addAttribute('position', new THREE.BufferAttribute(floatVertices, 3));
        geom.addAttribute('color', new THREE.BufferAttribute(floatColors, 3));
        geom.setIndex(new THREE.BufferAttribute(intIndices, 1));
        geom.computeVertexNormals();

        //model_renderer = new THREE.Mesh(geom, render_material);

        model_renderer = THREE.SceneUtils.createMultiMaterialObject(geom, [
            render_material,
            render_material_wireframe
        ]);

        new THREE.Mesh(geom, picking_material);

        scene.add(model_renderer);

        model_picker = new THREE.Mesh(geom, picking_material);
        pickingScene.add(model_picker);
    }

    this.enableYLimit = function() {
        yLimitEnabled = true;
    }

    this.enableIDLimit = function() {
        idLimitEnabled = true;
    }

    this.enableHoverDetection = function() {
        hoverDetectionEnabled = true;
    }

    this.setBlockList = function(blockList_) {
        blockList = blockList_;
        selectBox.visible = false;

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

        var gridSize = Math.floor(Math.max(maxX, maxZ) / 2) + 2

        gridXZ = new THREE.GridHelper(gridSize, 1);
        scene.add(gridXZ);
    }

    this.onBlockSelected = function(x, y, z, block) {
        console.log("Selected Block at X: " + x + " Y: " + y + " Z: " + z);
    }

    this.setHighlightBlockPosition = function(x,y,z) {
        selectBox.visible = true;
        selectBox.position.set(x+selectBoxHalfDim-maxX / 2,y+selectBoxHalfDim,z+selectBoxHalfDim-maxZ / 2);
    }

    this.onBlockHover = function(x, y, z, block) {
        this.setHighlightBlockPosition(x,y,z);
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

    this.getBlockList = function() {
        return blockList;
    }

    this.incrementMaxBlockID = function() {
        if (typeof blockList == "undefined") {
            return;
        }
        var coords = getCoords(maxBlockID);
        var x = coords.x;
        var y = coords.y;
        var z = coords.z + 1;
        for (; y < maxY; y++) {
            for (; x < maxX; x++) {
                for (; z < maxZ; z++) {
                    if (typeof blockList[x][y][z] !== "undefined" && blockList[x][y][z] != null) {
                        maxBlockID = getBlockID(x, y, z);
                        this.refresh();
                        return;
                    }
                }
                z = 0;
            }
            x = 0;
        }
    }

    this.decrementMaxBlockID = function() {
        if (typeof blockList == "undefined") {
            return;
        }
        var coords = getCoords(maxBlockID);
        var x = coords.x;
        var y = coords.y;
        var z = coords.z - 1;
        for (; y >= 0; y--) {
            for (; x >= 0; x--) {
                for (; z >= 0; z--) {
                    if (typeof blockList[x][y][z] !== "undefined" && blockList[x][y][z] != null) {
                        maxBlockID = getBlockID(x, y, z);
                        this.refresh();
                        return;
                    }
                }
                z = maxZ - 1;
            }
            x = maxX - 1;
        }
    }

    this.resetMaxBlockID = function() {
        maxBlockID = -1;
        this.incrementMaxBlockID();
        this.refresh();
    }

    function getBlockID(x, y, z) {
        return ((y & 0xFF) << 16) | ((x & 0xFF) << 8) | (z & 0xFF);
    }

    function getCoords(id) {
        return {
            y: id >> 16,
            x: (id >> 8) & 0xFF,
            z: (id & 0xFF)
        }
    }

    function pick(mouseDown) {

        //render the picking scene off-screen

        renderer.render(pickingScene, camera, pickingTexture);

        //create buffer for reading single pixel
        var pixelBuffer = new Uint8Array(4);

        var rect = renderer.domElement.getBoundingClientRect();

        var texCoordX = mouse.x - rect.left;
        var texCoordY = pickingTexture.height - (mouse.y - rect.top);

        if (texCoordX < 0 || texCoordY < 0 || texCoordX > pickingTexture.width || texCoordY > pickingTexture.height)
            return;

        //read the pixel under the mouse from the texture
        renderer.readRenderTargetPixels(pickingTexture, texCoordX, texCoordY, 1, 1, pixelBuffer);

        //interpret the pixel as an ID
        var id = (pixelBuffer[0] << 16) | (pixelBuffer[1] << 8) | (pixelBuffer[2]);

        // This will need to be changed if the clear color is changed
        if (id != 0xFFFFFF) {
            var coords = getCoords(id);
            var x = coords.x;
            var y = coords.y;
            var z = coords.z;


            if (x >= 0 && x < maxX && y >= 0 && y < maxY && z >= 0 && z < maxZ) {
                if (typeof blockList[x][y][z] === "undefined" || blockList[x][y][z] == null)
                    console.log("Error: Picking selected non-existent block");
                else if (mouseDown)
                    scope.onBlockSelected(x, y, z, blockList[x][y][z]);
                else
                    scope.onBlockHover(x, y, z, blockList[x][y][z]);
            }

        }


    }

    function animate() {

        requestAnimationFrame(animate);

        renderer.render(scene, camera);

        if (hoverDetectionEnabled)
            pick(false);

        controls.update();

        //stats.update();

    }

    function onMouseDown(event) {
        if (event.button == THREE.MOUSE.LEFT)
            pick(true);
    }

    function onMouseMove(e) {
        mouse.x = e.clientX;
        mouse.y = e.clientY;
    }

    function onKeyDown(e) {
        if (idLimitEnabled && e.keyCode == 37) {
            // Left Arrow
            scope.decrementMaxBlockID();
            e.preventDefault();
        } else if (yLimitEnabled && e.keyCode == 38) {
            // Up Arrow
            scope.setYRenderCap(scope.getYRenderCap() + 1);
            e.preventDefault();
        } else if (idLimitEnabled && e.keyCode == 39) {
            // Right Arrow
            scope.incrementMaxBlockID();
            e.preventDefault();
        } else if (yLimitEnabled && e.keyCode == 40) {
            // Down Arrow
            scope.setYRenderCap(scope.getYRenderCap() - 1);
            e.preventDefault();
        }
    }
}
