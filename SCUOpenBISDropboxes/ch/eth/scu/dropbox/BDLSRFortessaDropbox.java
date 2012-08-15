package ch.eth.scu.dropbox;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;

/**
 * Dropbox to register BD LSR Fortessa datasets into openBIS
 * @author Aaron Ponti
 *
 */
public class BDLSRFortessaDropbox extends AbstractJavaDataSetRegistrationDropboxV2 {

	@Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
		// TODO Implement
    }
}
