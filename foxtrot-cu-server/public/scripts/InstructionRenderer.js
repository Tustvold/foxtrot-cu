InstructionRenderer = function(screen_width, screen_height, domElement) {
    this.renderer = new BlockListRenderer(screen_width, screen_height, domElement);
    this.renderer.enableIDLimit();
    this.renderer.resetMaxBlockID();

    this.setBlockList = function(blockList_) {
        this.renderer.setBlockList(blockList_);
        this.renderer.resetMaxBlockID();
    }
}
