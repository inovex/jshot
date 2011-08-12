repositories.remote << 'http://www.ibiblio.org/maven2'

require 'time'

THIS_VERSION = '0.1'
SWT_VERSION = '3.7'
SWT_DROP = "R-#{SWT_VERSION}-201106131736"
SWT_ARCH = [:linux, :x86]

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

layout = Layout.new
layout[:source, :main, :java] = 'src'
layout[:source, :main, :resources] = 'conf'
layout[:source, :test, :java] = 'test'

define 'jshot', :layout=>layout do
  eclipse.natures 'org.eclipse.jdt.core.javanature'
  eclipse.builders 'org.eclipse.jdt.core.javabuilder'
  run.using :main => "de.inovex.jshot.JShot"


  # build up the filename

  @swt_arch = swt_archs[SWT_ARCH[0]][SWT_ARCH[1]]

 # download(artifact("org.eclipse.swt:swt-#{selected_swt_arch}:zip:#{SWT_VERSION}")=>swt_mirror)

  compile.with(
		"org.eclipse.swt:swt-#{@swt_arch}:jar:#{SWT_VERSION}"
	)

  project.group = 'de.inovex'
  project.version = THIS_VERSION
  package :sources
  package :javadoc

  package(:jar).with :manifest=>
  { 
    'Project' => project.id,
    'Copyright' => 'inovex GmbH (C) 2011',
    'Version' => THIS_VERSION,
    'Creation' => Time.now.strftime("%a, %d %b %Y %H:%M:%S %z"),
    'Main-Class' => 'de.inovex.jshot.JShot'
  }
  
  # define a task to test downloading
  # http://buildr.apache.org/artifacts.html
  
  task :swt do
    swt_filename = "swt-#{SWT_VERSION}-#{@swt_arch}"
    swt_url="http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/eclipse/downloads/drops/#{SWT_DROP}/#{swt_filename}.zip"
    swt_zip = download("target/#{swt_filename}.zip"=>swt_url)
    swt_jar = file("target/#{swt_filename}/swt.jar"=>unzip("target/#{swt_filename}"=>swt_zip))
    swt = artifact("org.eclipse.swt:swt-#{@swt_arch}:jar:#{SWT_VERSION}").from(swt_jar)
    install(swt).invoke()
  end
  
=begin 
  task :download do
    # download artifact
    # http://ruby-doc.org/stdlib/libdoc/net/http/rdoc/classes/Net/HTTP.html
    puts "Downloading #{swt_filename}.zip"
    require 'net/http'
    Net::HTTP.start("ftp-stud.fht-esslingen.de") do |http|
      file = open("#{swt_filename}.zip", 'wb')
      begin
        http.request_get("/pub/Mirrors/eclipse/eclipse/downloads/drops/#{SWT_DROP}/#{swt_filename}.zip") do |resp|
          resp.read_body do |segment|
            print "."
            file.write(segment)
          end
          print "\n"
        end
      ensure
        file.close()
      end
    end
    
    # http://rubyzip.sourceforge.net/
    require 'zip/zip'
    Zip::ZipFile.open("#{swt_filename}.zip") do |zipfile| 
      zipfile.extract('swt.jar', "#{swt_filename}.jar")
    end
    
 #   artifact("org.eclipse.swt:swt-#{selected_swt_arch}:jar:#{SWT_VERSION}")=>"file://#{swt_filename}.jar"

    ## extract jar from artifact
    ## install jar into local maven repository
    puts swt_mirror
  end
=end
  
end

