var packager = require('electron-packager');
var options = {
    'arch': 'ia32',
    'platform': 'win32',
    'dir': './',
    'app-copyright': 'Kolja Wilcke',
    'app-version': '0.0.1',
    'asar': true,
    'icon': '.resources/public/icon/comicexmachina.icns',
    'name': 'ComicExMachina',
    'ignore': ['./releases', './.git'],
    'out': './releases',
    'overwrite': true,
    'prune': true,
    'version': '0.0.1',
    'version-string':{
      'CompanyName': 'Kolja Wilcke',
      'FileDescription': 'Create Comics',
      'OriginalFilename': 'ComicExMachina',
      'ProductName': 'Comic Ex Machina',
      'InternalName': 'ComicExMachina'
    }
};
packager(options, function done_callback(err, appPaths) {
    console.log(err);
    console.log(appPaths);
});
