package se.yverling.wearto.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import se.yverling.wearto.R

fun errorTryAgainDialog(
        context: Context,
        titleResourceId: Int,
        messageResourceId: Int,
        listener: DialogInterface.OnClickListener): AlertDialog {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(titleResourceId)
            .setMessage(context.getString(messageResourceId))
            .setCancelable(true)
            .setPositiveButton(context.getString(R.string.try_again_button), listener)
            .setNegativeButton(context.getString(R.string.cancel_button), { _, _ -> })
    return builder.create()
}

fun warningMessageDialog(
        context: Context,
        titleResourceId: Int,
        messageResourceId: Int,
        listener: DialogInterface.OnClickListener): AlertDialog {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(titleResourceId)
            .setMessage(context.getString(messageResourceId))
            .setCancelable(true)
            .setPositiveButton(context.getString(R.string.ok_button), listener)
            .setNegativeButton(context.getString(R.string.cancel_button), { _, _ -> })
    return builder.create()
}
