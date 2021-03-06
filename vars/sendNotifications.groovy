#!/usr/bin/env groovy

import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper

/**
 * Send notifications based on build status string
 */
def call(RunWrapper currentBuild) {
  // build status of null means successful
  def buildStatus =  currentBuild.result ?: 'SUCCESS'
  def previousBuildStatus =  currentBuild.previousBuild?.result ?: 'SUCCESS'

  // Default values
  def colorCode = '#A20E10'
  def subject = ''

  if (buildStatus == 'ABORTED') {
    colorCode = '#A2A2A2'
    if (previousBuildStatus != 'SUCCESS') {
      subject = 'Aborted'
    }
  } else if (buildStatus == 'FAILURE') {
    colorCode = '#A20E10'
    if (previousBuildStatus == 'FAILURE') {
      subject = 'Still failing'
    } else {
      subject = 'Broken'
    }
  } else if (buildStatus == 'UNSTABLE') {
    colorCode = '#E5AD00'
    if (previousBuildStatus == 'UNSTABLE') {
      subject = 'Still unstable'
    } else {
      subject = "Unstable"
    }
  } else { // buildStatus == "SUCCESS"
    colorCode = '#34B787'
    if (previousBuildStatus != 'SUCCESS') {
      subject = "Back to normal"
    }
  }
  
  if (subject != '') {
    slackSend (color: colorCode, message: "*${subject}*: <${env.BUILD_URL}|${env.JOB_NAME} #${env.BUILD_NUMBER}>, duration: ${currentBuild.durationString.minus(" and counting")} (see <${env.BUILD_URL}console|logs tail>, or <${env.BUILD_URL}consoleFull|full logs>)")
    emailext (
      subject: "${subject}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
      body: """
        <p>See build result at <<a href='${env.BUILD_URL}'>${env.BUILD_URL}</a>></p>
        <p>See console output at <<a href='${env.BUILD_URL}console'>${env.BUILD_URL}console</a>></p>
        <p>See full console output at <<a href='${env.BUILD_URL}consoleFull'>${env.BUILD_URL}consoleFull</a>></p>
        <p>----</p>
        <pre style="white-space: pre-wrap;">${currentBuild.rawBuild.getLog(128).join('<br/>')}</pre>
      """,
      recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
    )
  }

  // Send notifications
  
}