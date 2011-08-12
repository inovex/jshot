repositories.remote << 'http://www.ibiblio.org/maven2'

require 'time'

SWT_VERSION = '3.7'
SWT_DROP = "R-#{SWT_VERSION}-201106131736"
ARCH = eval(File.read("arch"))

swt_archs = {
  :linux => {
    :x86 => "gtk-linux-x86",
    :x86_64 => "gtk-linux-x86_64"
    },
  :windows => {
    :x86 => "win32-win32-x86",
    :x86_64 => "win32-win32-x86_64"
    },
  :mac => {
    :x86 => "cocoa-macosx",
    :x86_64 => "cocoa-macosx-x86_64"
  }
}

SWT_ARCH = swt_archs[ARCH[0]][ARCH[1]]
THIS_VERSION = "0.1-#{SWT_ARCH}"

layout = Layout.new
layout[:source, :main, :java] = 'src'
layout[:source, :main, :resources] = 'conf'
layout[:source, :test, :java] = 'test'

define 'jshot', :layout=>layout do
  eclipse.natures 'org.eclipse.jdt.core.javanature'
  eclipse.builders 'org.eclipse.jdt.core.javabuilder'
  run.using :main => "de.inovex.jshot.JShot"

  compile.with(
		"org.eclipse.swt:swt-#{SWT_ARCH}:jar:#{SWT_VERSION}"
	)

  project.group = 'de.inovex'
  project.version = THIS_VERSION
  package :sources
  package :javadoc

=begin
    JarInJarLoader Configuration
    'Class-Path' => ". artifacts/*.jar",
    'Main-Class' => 'de.inovex.jshot.JShot'
    
    TODO use subfolder for libraries
    Paths that doesn't work for the Rsrc-Class-Path are
    - jar:rsrc://artifacts!/swt-gtk-linux-x86-3.7.jar
    - ./artifacts/swt-gtk-linux-x86-3.7.jar
    - artifacts/swt-gtk-linux-x86-3.7.jar
=end

  package(:jar).with :manifest=>
  { 
    'Project' => project.id,
    'Copyright' => 'inovex GmbH (C) 2011',
    'Version' => THIS_VERSION,
    'Creation' => Time.now.strftime("%a, %d %b %Y %H:%M:%S %z"),
    "Rsrc-Class-Path" => "./ swt-#{SWT_ARCH}-#{SWT_VERSION}.jar",
    "Class-Path"  => ".",
    "Rsrc-Main-Class" => "de.inovex.jshot.JShot",
    "Main-Class" => "org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader"
  }
  
  package(:jar).include([artifacts("org.eclipse.swt:swt-#{SWT_ARCH}:jar:#{SWT_VERSION}")], :path => '.')
 
  # http://buildr.apache.org/artifacts.html#install_upload
  # file task for downloading swt
  task :swt do
    swt_filename = "swt-#{SWT_VERSION}-#{SWT_ARCH}"
    swt_url="http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/eclipse/downloads/drops/#{SWT_DROP}/#{swt_filename}.zip"
    swt_zip = download("target/#{swt_filename}.zip"=>swt_url)
    swt_jar = file("target/#{swt_filename}/swt.jar"=>unzip("target/#{swt_filename}"=>swt_zip))
    swt = artifact("org.eclipse.swt:swt-#{SWT_ARCH}:jar:#{SWT_VERSION}").from(swt_jar)
    install(swt).invoke()
  end
  
end

