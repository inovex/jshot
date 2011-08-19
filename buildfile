repositories.remote << 'http://www.ibiblio.org/maven2'
repositories.remote << 'https://github.com/rjenster/mvn-repo/tree/master/releases'

require 'time'

SWT_VERSION = '3.7'
SWT_DROP = "R-#{SWT_VERSION}-201106131736"
ARCH = eval(File.read("arch"))

SWT_ARCHS = {
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

SWT_ARCH = SWT_ARCHS[ARCH[0]][ARCH[1]]
SWT_DEVELOP = "org.eclipse.swt:swt-#{SWT_ARCH}:jar:#{SWT_VERSION}"
PROJECT_VERSION = "0.1"

layout = Layout.new
layout[:source, :main, :java] = 'src'
layout[:source, :main, :resources] = 'conf'
layout[:source, :test, :java] = 'test'

define 'jshot', :layout=>layout do
  eclipse.natures 'org.eclipse.jdt.core.javanature'
  eclipse.builders 'org.eclipse.jdt.core.javabuilder'
  run.using :main => "de.inovex.jshot.JShot"

  project.group = 'de.inovex'
  project.version = "#{PROJECT_VERSION}-#{SWT_ARCH}"
#  package :sources
#  package :javadoc

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

  def swt_download(swt_arch)
    # http://buildr.apache.org/artifacts.html#install_upload
    # file task for downloading swt  
    swt_filename = "swt-#{SWT_VERSION}-#{swt_arch}"
    url="http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/eclipse/downloads/drops/#{SWT_DROP}/#{swt_filename}.zip"
    zip = download("target/#{swt_filename}.zip"=>url)
    jar = file("target/#{swt_filename}/swt.jar"=>unzip("target/#{swt_filename}"=>zip))
    spec = "org.eclipse.swt:swt-#{swt_arch}:jar:#{SWT_VERSION}"
    artifact = artifact(spec).from(jar)
    
    if not artifact.exist?      
      puts "Downloading SWT for arch[#{swt_arch}]"
      artifact.install
    end
    # clear the artifact, to prevent it to be downloaded again
    artifact.clear
  end
  
  # download the SWT library required for development as specified in the 'arch' file
  swt_download(SWT_ARCH)
  
  # add the SWT development library to the compile time dependencies
  compile.with(SWT_DEVELOP, 'eu.jenster.helper:helper-swt:jar:0.1')
  
  
  # package the programm for different architectures
  # @param swt_arch must be a value of #SWT_ARCHS
  def package_swt(swt_arch)
    
    project.version = "#{PROJECT_VERSION}-#{swt_arch}"
    
    puts "Packaging project for swt arch[#{swt_arch}]"
    
    swt_download(swt_arch)
    
    package(:jar).with :manifest=>
    { 
      'Project' => project.id,
      'Copyright' => 'inovex GmbH (C) 2011',
      'Version' => project.version,
      'Creation' => Time.now.strftime("%a, %d %b %Y %H:%M:%S %z"),
      "Rsrc-Class-Path" => "./ swt-#{swt_arch}-#{SWT_VERSION}.jar",
      "Class-Path"  => ".",
      "Rsrc-Main-Class" => "de.inovex.jshot.JShot",
      "Main-Class" => "org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader"
    }
      
    # remove SWT added for development from compile dependencies
    compile.dependencies.each_with_index do |dep, i|
      compile.dependencies.delete_at(i) if dep.to_spec == SWT_DEVELOP
    end

    swt_package_spec = "org.eclipse.swt:swt-#{swt_arch}:jar:#{SWT_VERSION}"
    
    compile.with(
		    swt_package_spec
	  )
    
    # add SWT library for given swt architecture to compile-time dependencies
    package(:jar).include([artifacts(swt_package_spec)], :path => '.')
    
    # run the package task
    Rake::Task[:package].invoke
  end
  
  task :package_swt_all do
    swt_archs.each_value do |system|
      system.each_value do |arch|
        swt_package(arch)
      end
    end
  end
  
  task :package_swt, [:system, :arch] do |t, args|
    # get the SWT architecture from the task arguments
    arch = SWT_ARCHS[args.system.to_sym][args.arch.to_sym]
    package_swt(arch)
  end
  
end

