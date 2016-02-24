InstructionRenderer = function(screen_width, screen_height, domElement) {
    var renderer = new BlockListRenderer(screen_width, screen_height, domElement);
    renderer.enableIDLimit();
    renderer.resetMaxBlockID();

    this.setBlockList = function(blockList_) {
        renderer.setBlockList(blockList_);
        renderer.resetMaxBlockID();
    }
}
