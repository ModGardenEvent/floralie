let
  nixpkgsVer = "b43c397f6c213918d6cfe6e3550abfe79b5d1c51";
  pkgs = import (fetchTarball "https://github.com/NixOS/nixpkgs/archive/${nixpkgsVer}.tar.gz") { config = {}; overlays = []; };
  libs = with pkgs; [
    libpulseaudio
    libGL
    glfw
    openal
    stdenv.cc.cc.lib
  ];
in pkgs.mkShell {
  name = "glowcase";

  buildInputs = with pkgs; [
    jdk21
  ] ++ libs;

  LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath libs;
}
