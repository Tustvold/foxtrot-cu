InstructionRenderer = function(screen_width, screen_height, domElement, labelDom) {
    this.renderer = new BlockListRenderer(screen_width, screen_height, domElement);
    this.renderer.enableIDLimit();
    this.renderer.resetMaxBlockID();

    this.setBlockList = function(blockList_) {
        this.renderer.setBlockList(blockList_);
        this.renderer.resetMaxBlockID();
    }

    this.setActive = function(active_) {
        this.renderer.setActive(active_);
    }

    var setMaxBlockIDSuper = this.renderer.setMaxBlockID;
    this.renderer.setMaxBlockID = function(maxBlockID_) {
        var coords = this.getCoords(maxBlockID_);
        labelDom.textContent = "Block: (X: " + coords.x + ", Y: " + coords.y + ", Z: " + coords.z + ")";

        this.setHighlightBlockPosition(coords.x, coords.y, coords.z);
        setMaxBlockIDSuper.call(this, maxBlockID_);
    }

    var finished = false;

    var incrementMaxBlockIDSuper = this.renderer.incrementMaxBlockID;
    this.renderer.incrementMaxBlockID = function() {
        if (!incrementMaxBlockIDSuper.call(this)) {
            this.setHighlightBlockVisible(false);
            labelDom.textContent = "Model Finished";
            finished = true;
        }
    }

    var decrementMaxBlockIDSuper = this.renderer.decrementMaxBlockID;
    this.renderer.decrementMaxBlockID = function() {
        if (finished) {
            this.setMaxBlockID(this.getMaxBlockID());
            finished = false;
        } else {
            decrementMaxBlockIDSuper.call(this);
        }
    }
}
