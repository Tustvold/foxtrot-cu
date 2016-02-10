function createDelegate(object, method) {
    var shim = function() {
        return method.apply(object, arguments);
    }
    return shim;
}
