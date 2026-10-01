package se.yverling.wearto.mobile.app

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.io.File

private class BackupRulesTest {

    @Test
    fun `dataExtractionRules should exclude token ciphertext and keyset files`() {
        val rulesFile = File("src/main/res/xml/data_extraction_rules.xml")
        rulesFile.exists().shouldBeTrue()

        val content = rulesFile.readText()
        content shouldContain "<cloud-backup>"
        content shouldContain "<device-transfer>"
        content shouldContain """<exclude domain="file" path="datastore/token_v1.json" />"""
        content shouldContain """<exclude domain="file" path="datastore/token.preferences_pb" />"""
        content shouldContain """<exclude domain="sharedpref" path="wearto_token_keyset_prefs.xml" />"""
    }

    @Test
    fun `backupRules should exclude token ciphertext and keyset files`() {
        val rulesFile = File("src/main/res/xml/backup_rules.xml")
        rulesFile.exists().shouldBeTrue()

        val content = rulesFile.readText()
        content shouldContain "<full-backup-content>"
        content shouldContain """<exclude domain="file" path="datastore/token_v1.json" />"""
        content shouldContain """<exclude domain="file" path="datastore/token.preferences_pb" />"""
        content shouldContain """<exclude domain="sharedpref" path="wearto_token_keyset_prefs.xml" />"""
    }

    @Test
    fun `manifest should configure backup rules attributes`() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        manifestFile.exists().shouldBeTrue()

        val content = manifestFile.readText()
        content shouldContain """android:dataExtractionRules="@xml/data_extraction_rules""""
        content shouldContain """android:fullBackupContent="@xml/backup_rules""""
    }
}
