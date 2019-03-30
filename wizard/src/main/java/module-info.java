/**
 * Tools for creating guided interview style dialogs.
 */
module com.metsci.glimpse.wizard
{
	exports com.metsci.glimpse.wizard.page;
	exports com.metsci.glimpse.wizard.tree;
	exports com.metsci.glimpse.wizard.listener;
	exports com.metsci.glimpse.wizard;
	exports com.metsci.glimpse.wizard.error;

	requires transitive com.metsci.glimpse.docking;
}
