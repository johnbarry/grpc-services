
//java { sourceCompatibility = JavaVersion.VERSION_11 }
repositories {
	mavenCentral()
}

allprojects {
	apply(plugin="idea")
	defaultTasks ("clean", "build")
	group = "org.jpb"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}
