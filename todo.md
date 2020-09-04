
[x] draw-area for every Panel
[ ] refactor: check for duplicate nodes at the time they are added (avoid one recursion level)
[x] implement Tools bar
[ ] hide mouse and show cursor specific to active tool
[ ] save function
[ ] refactor: Pages and Panels are not Vectors, but maps (so their ID is not dependent on their position in the vector -> easier removal)
[ ] Layers:
    [ ] Background-layer, can draw panels everywhere on the page
    [ ] Overlay Layer for Circles (with ouline that interacts with all Panels)
    [ ] Layer has list of Panels which can be deleted individually.
    [ ] Support different line-styles (and no-outline) on per-layer (per Panel?) basis
    [ ] Support different gutter-width per layer.
[ ] Allow for Vertices to be moved
[x] Bugfix: when removing a cell (a whole Panel?) there can be a nil/nil pair in global cells hash.
[ ] Empty panels need to be removed.

--
[ ] move drawing content tool (aka "hand" tool)
[ ] drawing tool prefs: color, line-width, line-style
[ ] panel-tool prefs: cursor size
